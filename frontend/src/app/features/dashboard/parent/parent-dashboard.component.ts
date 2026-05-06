import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription, filter, forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ParentService } from '../../../core/services/parent.service';
import { LucideAngularModule } from 'lucide-angular';
import { Child } from '../../../core/models/child.model';
import { Appointment } from '../../../core/models/appointment.model';
import { GrowthMeasurement, DietaryRestriction, NutritionalPlan } from '../../../core/models/treatment.model';
import { PrescriptionService } from '../../prescription/services/prescription.service';
import { Prescription } from '../../prescription/models/prescription.model';

@Component({
  selector: 'app-parent-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './parent-dashboard.component.html',
  styleUrl: './parent-dashboard.component.scss',
})
export class ParentDashboardComponent implements OnInit, OnDestroy {
  currentUser$ = this.auth.currentUser$;
  mobileMenuOpen = false;
  userMenuOpen = false;
  activeTab = 'dashboard';
  childRouteActive = false;
  private routeSub!: Subscription;

  loading = true;
  stats = { children: 0, appointments: 0 };
  children: Child[] = [];
  appointments: Appointment[] = [];
  filteredAppointments: Appointment[] = [];
  availableDoctors: any[] = [];
  recentPrescriptions: Prescription[] = [];

  searchTerm = '';
  statusFilter = 'ALL';
  dateFilter = 'ALL';
  sortBy: 'date' | 'status' = 'date';
  sortOrder: 'asc' | 'desc' = 'asc';
  viewMode: 'grid' | 'list' = 'grid';
  
  appointmentStats = {
    total: 0,
    pending: 0,
    accepted: 0,
    completed: 0
  };
  
  selectedAppointment: Appointment | null = null;
  showAppointmentDetailsModal = false;

  showChildModal = false;
  editingChild: Child | null = null;
  childForm!: FormGroup;
  childSaving = false;

  showApptModal = false;
  apptForm!: FormGroup;
  apptSaving = false;

  showDoctorModal = false;
  selectedChildForDoctor: Child | null = null;

  selectedChildId = '';
  measurements: GrowthMeasurement[] = [];
  restrictions: DietaryRestriction[] = [];
  plans: NutritionalPlan[] = [];
  growthSubTab: 'measurements' | 'restrictions' | 'plans' = 'measurements';

  showMeasurementModal = false;
  editingMeasurement: GrowthMeasurement | null = null;
  measurementForm!: FormGroup;
  measurementSaving = false;

  showRestrictionModal = false;
  editingRestriction: DietaryRestriction | null = null;
  restrictionForm!: FormGroup;
  restrictionSaving = false;

  alertMsg = '';
  alertType: 'success' | 'error' = 'success';

  constructor(
    private auth: AuthService,
    private router: Router,
    private parentService: ParentService,
    private prescriptionService: PrescriptionService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.checkChildRoute(this.router.url);
    this.routeSub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(e => this.checkChildRoute(e.urlAfterRedirects));

    this.childForm = this.fb.group({
      fullName: ['', Validators.required],
      dateOfBirth: ['', Validators.required],
      gender: ['', Validators.required],
      notes: ['']
    });

    this.apptForm = this.fb.group({
      childId: ['', Validators.required],
      doctorId: ['', Validators.required],
      dateTime: ['', Validators.required],
      reason: ['', Validators.required],
      parentNotes: ['']
    });

    this.measurementForm = this.fb.group({
      date: ['', Validators.required],
      weight: ['', [Validators.required, Validators.min(0.1)]],
      height: ['', [Validators.required, Validators.min(1)]],
      headCircumference: [''],
      notes: ['']
    });

    this.restrictionForm = this.fb.group({
      type: ['', Validators.required],
      allergen: ['', Validators.required],
      severity: ['', Validators.required],
      description: [''],
      notes: ['']
    });

    this.loadData();
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
  }

  private checkChildRoute(url: string): void {
    this.childRouteActive = url !== '/parent' && url.startsWith('/parent/');
  }

  loadData(): void {
    this.loading = true;
    forkJoin({
      dashboard: this.parentService.getDashboard(),
      children: this.parentService.getChildren(),
      appointments: this.parentService.getAppointments(),
      doctors: this.parentService.getAvailableDoctors()
    }).subscribe({
      next: ({ dashboard, children, appointments, doctors }) => {
        this.stats = { children: (dashboard as any).childrenCount || 0, appointments: (dashboard as any).appointmentsCount || 0 };
        this.children = children;
        this.appointments = appointments;
        this.availableDoctors = doctors;
        this.calculateAppointmentStats();
        this.applyAppointmentFilters();
        this.loading = false;
        this.loadRecentPrescriptions();
      },
      error: () => { this.loading = false; }
    });
  }

  loadRecentPrescriptions(): void {
    if (this.children.length === 0) {
      this.recentPrescriptions = [];
      return;
    }
    
    const prescriptionRequests = this.children.map(child => 
      this.prescriptionService.getPrescriptionsByPatient(child.id!)
    );
    
    forkJoin(prescriptionRequests).subscribe({
      next: (results) => {
        const allPrescriptions: Prescription[] = [];
        results.forEach(response => {
          if (response.data && Array.isArray(response.data)) {
            allPrescriptions.push(...response.data);
          }
        });
        
        this.recentPrescriptions = allPrescriptions
          .sort((a, b) => new Date(b.datePrescription).getTime() - new Date(a.datePrescription).getTime())
          .slice(0, 5);
      },
      error: (err) => {
        console.error('Error loading prescriptions:', err);
        this.recentPrescriptions = [];
      }
    });
  }

  getPrescriptionStatusClass(status: string): string {
    const statusMap: any = {
      'ACTIVE': 'success',
      'EXPIREE': 'danger',
      'TERMINEE': 'muted',
      'ANNULEE': 'warning',
      'RENOUVELEE': 'info'
    };
    return statusMap[status] || 'muted';
  }

  setTab(tab: string): void {
    this.activeTab = tab;
    this.mobileMenuOpen = false;
    if (tab === 'prescriptions') {
      this.router.navigate(['/parent/prescriptions']);
      return;
    }
    if (tab === 'treatments') {
      this.router.navigate(['/parent/treatments']);
      return;
    }
    if (tab === 'lab-results') {
      this.router.navigate(['/parent/lab-results']);
      return;
    }
    if (tab === 'adherence') {
      this.router.navigate(['/parent/adherence']);
      return;
    }
    if (tab === 'progress') {
      this.router.navigate(['/parent/progress']);
      return;
    }
    if (tab === 'consultations') {
      this.router.navigate(['/parent/consultations']);
      return;
    }
    if (tab === 'messages') {
      this.router.navigate(['/parent/messages']);
      return;
    }
    if (this.childRouteActive) this.router.navigate(['/parent']);
    if (tab === 'growth' && this.children.length > 0 && !this.selectedChildId) {
      this.selectedChildId = this.children[0].id!;
      this.loadGrowthData();
    }
  }

  toggleMobileMenu(): void { this.mobileMenuOpen = !this.mobileMenuOpen; }
  toggleUserMenu(): void { this.userMenuOpen = !this.userMenuOpen; }
  logout(): void { this.auth.logout(); }

  openAddChild(): void {
    this.editingChild = null;
    this.childForm.reset();
    this.showChildModal = true;
  }

  openEditChild(child: Child): void {
    this.editingChild = child;
    this.childForm.patchValue({
      fullName: child.fullName,
      dateOfBirth: child.dateOfBirth,
      gender: child.gender,
      notes: child.notes
    });
    this.showChildModal = true;
  }

  saveChild(): void {
    if (this.childForm.invalid) return;
    this.childSaving = true;
    const data = this.childForm.value;
    const obs = this.editingChild
      ? this.parentService.updateChild(this.editingChild.id!, data)
      : this.parentService.addChild(data);
    obs.subscribe({
      next: () => {
        this.showChildModal = false;
        this.childSaving = false;
        this.showAlert(this.editingChild ? 'Child updated' : 'Child added', 'success');
        this.loadData();
      },
      error: () => { this.childSaving = false; this.showAlert('Failed to save child', 'error'); }
    });
  }

  deleteChild(child: Child): void {
    if (!confirm('Delete ' + child.fullName + '?')) return;
    this.parentService.deleteChild(child.id!).subscribe({
      next: () => { this.showAlert('Child deleted', 'success'); this.loadData(); },
      error: () => this.showAlert('Failed to delete child', 'error')
    });
  }

  openDoctorModal(child: Child): void {
    this.selectedChildForDoctor = child;
    this.showDoctorModal = true;
  }

  assignDoctor(doctorId: string): void {
    if (!this.selectedChildForDoctor) return;
    this.parentService.assignDoctor(this.selectedChildForDoctor.id!, doctorId).subscribe({
      next: () => { this.showDoctorModal = false; this.showAlert('Doctor assigned', 'success'); this.loadData(); },
      error: () => this.showAlert('Failed to assign doctor', 'error')
    });
  }

  removeDoctor(child: Child, doctorId: string): void {
    this.parentService.removeDoctor(child.id!, doctorId).subscribe({
      next: () => { this.showAlert('Doctor removed', 'success'); this.loadData(); },
      error: () => this.showAlert('Failed to remove doctor', 'error')
    });
  }

  openApptModal(): void {
    this.apptForm.reset();
    this.showApptModal = true;
  }

  saveAppointment(): void {
    if (this.apptForm.invalid) return;
    this.apptSaving = true;
    this.parentService.createAppointment(this.apptForm.value).subscribe({
      next: () => {
        this.showApptModal = false;
        this.apptSaving = false;
        this.showAlert('Appointment created', 'success');
        this.loadData();
      },
      error: () => { this.apptSaving = false; this.showAlert('Failed to create appointment', 'error'); }
    });
  }

  cancelAppointment(id: string): void {
    if (!confirm('Cancel this appointment?')) return;
    this.parentService.cancelAppointment(id).subscribe({
      next: () => { this.showAlert('Appointment cancelled', 'success'); this.loadData(); },
      error: () => this.showAlert('Failed to cancel appointment', 'error')
    });
  }

  onChildSelected(): void {
    this.loadGrowthData();
  }

  loadGrowthData(): void {
    if (!this.selectedChildId) return;
    forkJoin({
      measurements: this.parentService.getMeasurements(this.selectedChildId),
      restrictions: this.parentService.getRestrictions(this.selectedChildId),
      plans: this.parentService.getChildPlans(this.selectedChildId)
    }).subscribe({
      next: ({ measurements, restrictions, plans }) => {
        this.measurements = measurements;
        this.restrictions = restrictions;
        this.plans = plans;
      }
    });
  }

  openAddMeasurement(): void {
    this.editingMeasurement = null;
    this.measurementForm.reset();
    this.showMeasurementModal = true;
  }

  openEditMeasurement(m: GrowthMeasurement): void {
    this.editingMeasurement = m;
    this.measurementForm.patchValue({
      date: m.date,
      weight: m.weight,
      height: m.height,
      headCircumference: m.headCircumference || '',
      notes: m.notes || ''
    });
    this.showMeasurementModal = true;
  }

  saveMeasurement(): void {
    if (this.measurementForm.invalid || !this.selectedChildId) return;
    this.measurementSaving = true;
    const data = this.measurementForm.value;
    const obs = this.editingMeasurement
      ? this.parentService.updateMeasurement(this.selectedChildId, this.editingMeasurement.id!, data)
      : this.parentService.addMeasurement(this.selectedChildId, data);
    obs.subscribe({
      next: () => {
        this.showMeasurementModal = false;
        this.measurementSaving = false;
        this.showAlert(this.editingMeasurement ? 'Measurement updated' : 'Measurement added', 'success');
        this.loadGrowthData();
      },
      error: () => { this.measurementSaving = false; this.showAlert('Failed to save measurement', 'error'); }
    });
  }

  deleteMeasurement(m: GrowthMeasurement): void {
    if (!confirm('Delete this measurement?')) return;
    this.parentService.deleteMeasurement(this.selectedChildId, m.id!).subscribe({
      next: () => { this.showAlert('Measurement deleted', 'success'); this.loadGrowthData(); },
      error: () => this.showAlert('Failed to delete measurement', 'error')
    });
  }

  openAddRestriction(): void {
    this.editingRestriction = null;
    this.restrictionForm.reset();
    this.showRestrictionModal = true;
  }

  openEditRestriction(r: DietaryRestriction): void {
    this.editingRestriction = r;
    this.restrictionForm.patchValue({
      type: r.type,
      allergen: r.allergen,
      severity: r.severity,
      description: r.description || '',
      notes: r.notes || ''
    });
    this.showRestrictionModal = true;
  }

  saveRestriction(): void {
    if (this.restrictionForm.invalid || !this.selectedChildId) return;
    this.restrictionSaving = true;
    const data = this.restrictionForm.value;
    const obs = this.editingRestriction
      ? this.parentService.updateRestriction(this.selectedChildId, this.editingRestriction.id!, data)
      : this.parentService.addRestriction(this.selectedChildId, data);
    obs.subscribe({
      next: () => {
        this.showRestrictionModal = false;
        this.restrictionSaving = false;
        this.showAlert(this.editingRestriction ? 'Restriction updated' : 'Restriction added', 'success');
        this.loadGrowthData();
      },
      error: () => { this.restrictionSaving = false; this.showAlert('Failed to save restriction', 'error'); }
    });
  }

  deleteRestriction(r: DietaryRestriction): void {
    if (!confirm('Delete this restriction?')) return;
    this.parentService.deleteRestriction(this.selectedChildId, r.id!).subscribe({
      next: () => { this.showAlert('Restriction deleted', 'success'); this.loadGrowthData(); },
      error: () => this.showAlert('Failed to delete restriction', 'error')
    });
  }

  getSeverityClass(severity: string): string {
    const map: Record<string, string> = { LOW: 'info', MODERATE: 'warning', HIGH: 'danger', SEVERE: 'danger' };
    return map[severity] || '';
  }

  getDoctorName(doctorId: string): string {
    const d = this.availableDoctors.find(doc => doc.id === doctorId);
    return d ? d.fullName : doctorId;
  }

  getChildName(childId: string): string {
    const c = this.children.find(ch => ch.id === childId);
    return c ? c.fullName : childId;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'warning', ACCEPTED: 'success', REFUSED: 'danger',
      RESCHEDULED: 'info', COMPLETED: 'muted', CANCELLED: 'muted'
    };
    return map[status] || '';
  }
  
  getStatusIcon(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'clock', ACCEPTED: 'check-circle', REFUSED: 'x-circle',
      RESCHEDULED: 'calendar', COMPLETED: 'check-circle-2', CANCELLED: 'ban'
    };
    return map[status] || 'circle';
  }
  
  calculateAppointmentStats(): void {
    this.appointmentStats.total = this.appointments.length;
    this.appointmentStats.pending = this.appointments.filter(a => a.status === 'PENDING').length;
    this.appointmentStats.accepted = this.appointments.filter(a => a.status === 'ACCEPTED').length;
    this.appointmentStats.completed = this.appointments.filter(a => a.status === 'COMPLETED').length;
  }
  
  applyAppointmentFilters(): void {
    let filtered = [...this.appointments];
    
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(apt => 
        (apt.reason?.toLowerCase().includes(term)) ||
        (apt.parentNotes?.toLowerCase().includes(term)) ||
        (this.getChildName(apt.childId).toLowerCase().includes(term)) ||
        (this.getDoctorName(apt.doctorId).toLowerCase().includes(term))
      );
    }
    
    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(apt => apt.status === this.statusFilter);
    }
    
    if (this.dateFilter !== 'ALL') {
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      
      filtered = filtered.filter(apt => {
        const aptDate = new Date(apt.dateTime);
        const aptDay = new Date(aptDate.getFullYear(), aptDate.getMonth(), aptDate.getDate());
        
        switch (this.dateFilter) {
          case 'TODAY':
            return aptDay.getTime() === today.getTime();
          case 'WEEK':
            const weekFromNow = new Date(today);
            weekFromNow.setDate(weekFromNow.getDate() + 7);
            return aptDay >= today && aptDay <= weekFromNow;
          case 'MONTH':
            return aptDate.getMonth() === now.getMonth() && aptDate.getFullYear() === now.getFullYear();
          case 'PAST':
            return aptDay < today;
          case 'UPCOMING':
            return aptDay >= today;
          default:
            return true;
        }
      });
    }
    
    filtered.sort((a, b) => {
      let comparison = 0;
      
      if (this.sortBy === 'date') {
        comparison = new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime();
      } else if (this.sortBy === 'status') {
        comparison = (a.status || '').localeCompare(b.status || '');
      }
      
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });
    
    this.filteredAppointments = filtered;
  }
  
  onSearchChange(): void {
    this.applyAppointmentFilters();
  }
  
  onFilterChange(): void {
    this.applyAppointmentFilters();
  }
  
  onSortChange(): void {
    this.applyAppointmentFilters();
  }
  
  toggleSortOrder(): void {
    this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    this.applyAppointmentFilters();
  }
  
  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = 'ALL';
    this.dateFilter = 'ALL';
    this.sortBy = 'date';
    this.sortOrder = 'asc';
    this.applyAppointmentFilters();
  }
  
  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'grid' ? 'list' : 'grid';
  }
  
  showAppointmentDetails(apt: Appointment): void {
    this.selectedAppointment = apt;
    this.showAppointmentDetailsModal = true;
  }

  private showAlert(msg: string, type: 'success' | 'error'): void {
    this.alertMsg = msg;
    this.alertType = type;
    setTimeout(() => this.alertMsg = '', 4000);
  }
}
