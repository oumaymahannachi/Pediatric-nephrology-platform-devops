import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { forkJoin } from 'rxjs';
import { DoctorService } from '../../../../core/services/doctor.service';
import { Appointment } from '../../../../core/models/appointment.model';

@Component({
  selector: 'app-doctor-appointments',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './doctor-appointments.component.html',
  styleUrl: './doctor-appointments.component.scss'
})
export class DoctorAppointmentsComponent implements OnInit {
  loading = true;
  activeTab: 'all' | 'pending' = 'all';
  appointments: Appointment[] = [];
  pendingAppointments: Appointment[] = [];
  filteredAppointments: Appointment[] = [];
  
  searchTerm = '';
  statusFilter = 'ALL';
  dateFilter = 'ALL';
  sortBy: 'date' | 'status' = 'date';
  sortOrder: 'asc' | 'desc' = 'asc';
  
  viewMode: 'grid' | 'list' = 'grid';

  rescheduleId = '';
  rescheduleDate = '';
  rescheduleNotes = '';
  showRescheduleModal = false;
  
  selectedAppointment: Appointment | null = null;
  showDetailsModal = false;

  alertMsg = '';
  alertType: 'success' | 'error' = 'success';
  
  stats = {
    total: 0,
    pending: 0,
    accepted: 0,
    completed: 0
  };

  constructor(private doctorService: DoctorService) {}

  ngOnInit(): void { this.loadData(); }

  loadData(): void {
    this.loading = true;
    forkJoin({
      appointments: this.doctorService.getAppointments(),
      pending: this.doctorService.getPendingAppointments()
    }).subscribe({
      next: ({ appointments, pending }) => {
        this.appointments = appointments;
        this.pendingAppointments = pending;
        this.calculateStats();
        this.applyFilters();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }
  
  calculateStats(): void {
    this.stats.total = this.appointments.length;
    this.stats.pending = this.appointments.filter(a => a.status === 'PENDING').length;
    this.stats.accepted = this.appointments.filter(a => a.status === 'ACCEPTED').length;
    this.stats.completed = this.appointments.filter(a => a.status === 'COMPLETED').length;
  }

  applyFilters(): void {
    let filtered = [...this.appointments];
    
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(apt => 
        (apt.reason?.toLowerCase().includes(term)) ||
        (apt.parentNotes?.toLowerCase().includes(term))
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
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }
  
  onSortChange(): void {
    this.applyFilters();
  }
  
  toggleSortOrder(): void {
    this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    this.applyFilters();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = 'ALL';
    this.dateFilter = 'ALL';
    this.sortBy = 'date';
    this.sortOrder = 'asc';
    this.applyFilters();
  }
  
  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'grid' ? 'list' : 'grid';
  }
  
  showDetails(apt: Appointment): void {
    this.selectedAppointment = apt;
    this.showDetailsModal = true;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'warning', ACCEPTED: 'success', REFUSED: 'danger',
      RESCHEDULED: 'info', COMPLETED: 'primary', CANCELLED: 'secondary'
    };
    return map[status] || 'secondary';
  }
  
  getStatusIcon(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'clock', ACCEPTED: 'check-circle', REFUSED: 'x-circle',
      RESCHEDULED: 'calendar', COMPLETED: 'check-circle-2', CANCELLED: 'ban'
    };
    return map[status] || 'circle';
  }

  acceptAppointment(id: string): void {
    this.doctorService.acceptAppointment(id).subscribe({
      next: () => { this.showAlert('✓ Rendez-vous accepté avec succès', 'success'); this.loadData(); },
      error: () => this.showAlert('✗ Erreur lors de l\'acceptation', 'error')
    });
  }

  refuseAppointment(id: string): void {
    if (!confirm('Êtes-vous sûr de vouloir refuser ce rendez-vous?')) return;
    this.doctorService.refuseAppointment(id).subscribe({
      next: () => { this.showAlert('✓ Rendez-vous refusé', 'success'); this.loadData(); },
      error: () => this.showAlert('✗ Erreur lors du refus', 'error')
    });
  }

  completeAppointment(id: string): void {
    this.doctorService.completeAppointment(id).subscribe({
      next: () => { this.showAlert('✓ Rendez-vous marqué comme complété', 'success'); this.loadData(); },
      error: () => this.showAlert('✗ Erreur lors de la complétion', 'error')
    });
  }

  openReschedule(apt: Appointment): void {
    this.rescheduleId = apt.id!;
    this.rescheduleDate = '';
    this.rescheduleNotes = '';
    this.showRescheduleModal = true;
  }

  submitReschedule(): void {
    if (!this.rescheduleDate) return;
    this.doctorService.rescheduleAppointment(this.rescheduleId, this.rescheduleDate, this.rescheduleNotes).subscribe({
      next: () => {
        this.showRescheduleModal = false;
        this.showAlert('✓ Rendez-vous reporté avec succès', 'success');
        this.loadData();
      },
      error: () => this.showAlert('✗ Erreur lors du report', 'error')
    });
  }

  private showAlert(msg: string, type: 'success' | 'error'): void {
    this.alertMsg = msg;
    this.alertType = type;
    setTimeout(() => this.alertMsg = '', 4000);
  }
}
