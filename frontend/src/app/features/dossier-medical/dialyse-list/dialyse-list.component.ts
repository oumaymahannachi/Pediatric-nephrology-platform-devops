import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialysisService } from '../../../core/services/dialysis.service';
import { DialysisPrescription, DialysisSession } from '../../../core/models/dialysis.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-dialyse-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './dialyse-list.component.html',
  styleUrl: './dialyse-list.component.css'
})
export class DialyseListComponent implements OnInit, OnChanges {
  @Input() patientId!: string;
  
  loading = true;
  prescriptions: DialysisPrescription[] = [];
  sessions: DialysisSession[] = [];
  selectedPrescription: DialysisPrescription | null = null;
  currentUserId: string = '';
  
  showPrescriptionModal = false;
  showSessionModal = false;
  showRescheduleModal = false;
  prescriptionForm!: FormGroup;
  sessionForm!: FormGroup;
  rescheduleForm!: FormGroup;
  selectedSession: DialysisSession | null = null;
  
  dialysisTypes = [
    { value: 'HEMODIALYSIS', label: 'Hemodialysis' },
    { value: 'PERITONEAL', label: 'Peritoneal Dialysis' },
    { value: 'HEMOFILTRATION', label: 'Hemofiltration' },
    { value: 'HEMODIAFILTRATION', label: 'Hemodiafiltration' }
  ];

  constructor(
    private dialysisService: DialysisService,
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.initForms();
    this.authService.currentUser$.subscribe(user => {
      this.currentUserId = user?.id || '';
    });
  }

  ngOnInit(): void {
    if (this.patientId) {
      this.loadData();
    }
  }

  ngOnChanges(): void {
    if (this.patientId) {
      this.loadData();
    }
  }

  initForms(): void {
    this.prescriptionForm = this.fb.group({
      type: ['HEMODIALYSIS', Validators.required],
      frequencyPerWeek: [3, [Validators.required, Validators.min(1)]],
      sessionDurationMinutes: [240, [Validators.required, Validators.min(30)]],
      bloodFlowRate: [300],
      dialysateFlowRate: [500],
      anticoagulation: [''],
      vascularAccess: [''],
      notes: [''],
      startDate: ['', Validators.required]
    });

    this.sessionForm = this.fb.group({
      scheduledDate: ['', Validators.required]
    });

    this.rescheduleForm = this.fb.group({
      scheduledDate: ['', Validators.required]
    });
  }

  loadData(): void {
    this.loading = true;
    this.dialysisService.getPrescriptionsByPatient(this.patientId).subscribe({
      next: (prescriptions) => {
        this.prescriptions = prescriptions;
        if (prescriptions.length > 0) {
          this.loadSessions();
        } else {
          this.loading = false;
        }
      },
      error: () => { this.loading = false; }
    });
  }

  loadSessions(): void {
    this.dialysisService.getSessionsByPatient(this.patientId).subscribe({
      next: (sessions) => {
        this.sessions = sessions;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  openPrescriptionModal(prescription?: DialysisPrescription): void {
    if (prescription) {
      this.selectedPrescription = prescription;
      this.prescriptionForm.patchValue(prescription);
    } else {
      this.selectedPrescription = null;
      this.prescriptionForm.reset({ type: 'HEMODIALYSIS', frequencyPerWeek: 3, sessionDurationMinutes: 240 });
    }
    this.showPrescriptionModal = true;
  }

  savePrescription(): void {
    if (this.prescriptionForm.invalid) return;
    
    const prescriptionData = {
      ...this.prescriptionForm.value,
      patientId: this.patientId,
      medecinId: this.currentUserId
    };

    const request = this.selectedPrescription
      ? this.dialysisService.updatePrescription(this.selectedPrescription.id!, prescriptionData)
      : this.dialysisService.createPrescription(prescriptionData);

    request.subscribe({
      next: () => {
        this.showPrescriptionModal = false;
        this.loadData();
      },
      error: (err) => console.error('Error saving prescription:', err)
    });
  }

  openSessionModal(prescription: DialysisPrescription): void {
    this.selectedPrescription = prescription;
    const dateStr = prescription.startDate ? new Date(prescription.startDate).toISOString().slice(0, 16) : '';
    this.sessionForm.patchValue({ scheduledDate: dateStr });
    this.showSessionModal = true;
  }

  scheduleSession(): void {
    if (this.sessionForm.invalid || !this.selectedPrescription) return;

    const newStartDate = this.sessionForm.value.scheduledDate;
    
    const updatedPrescription = {
      ...this.selectedPrescription,
      startDate: newStartDate
    };

    this.dialysisService.updatePrescription(this.selectedPrescription.id!, updatedPrescription).subscribe({
      next: () => {
        this.showSessionModal = false;
        this.loadData();
      },
      error: (err) => console.error('Error updating prescription:', err)
    });
  }

  getSessionsForPrescription(prescriptionId: string): DialysisSession[] {
    return this.sessions.filter(s => s.prescriptionId === prescriptionId);
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      SCHEDULED: 'scheduled',
      IN_PROGRESS: 'in-progress',
      COMPLETED: 'completed',
      CANCELLED: 'cancelled',
      MISSED: 'missed'
    };
    return map[status] || 'scheduled';
  }

  openRescheduleModal(session: DialysisSession): void {
    this.selectedSession = session;
    const dateStr = new Date(session.scheduledDate!).toISOString().slice(0, 16);
    this.rescheduleForm.patchValue({ scheduledDate: dateStr });
    this.showRescheduleModal = true;
  }

  rescheduleSession(): void {
    if (this.rescheduleForm.invalid || !this.selectedSession) return;

    const newDate = this.rescheduleForm.value.scheduledDate;
    this.dialysisService.rescheduleSession(this.selectedSession.id!, newDate).subscribe({
      next: () => {
        this.showRescheduleModal = false;
        this.loadSessions();
      },
      error: (err) => console.error('Error rescheduling session:', err)
    });
  }

  cancelSessionConfirm(session: DialysisSession): void {
    if (!confirm('Are you sure you want to cancel this session?')) return;
    
    this.dialysisService.cancelSession(session.id!, 'Cancelled by doctor').subscribe({
      next: () => {
        this.loadSessions();
      },
      error: (err) => console.error('Error cancelling session:', err)
    });
  }
}
