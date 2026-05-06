import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { DoctorService } from '../../../../core/services/doctor.service';
import { Appointment } from '../../../../core/models/appointment.model';

@Component({
  selector: 'app-doctor-medical-records',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './doctor-medical-records.component.html',
  styleUrl: './doctor-medical-records.component.scss'
})
export class DoctorMedicalRecordsComponent implements OnInit {
  loading = true;
  acceptedAppointments: Appointment[] = [];
  selectedAppointment: Appointment | null = null;
  showRecordModal = false;
  recordForm!: FormGroup;
  
  alertMsg = '';
  alertType: 'success' | 'error' = 'success';

  constructor(
    private doctorService: DoctorService,
    private fb: FormBuilder
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadAcceptedAppointments();
  }

  initForm(): void {
    this.recordForm = this.fb.group({
      observationsCliniques: ['', Validators.required],
      diagnostic: ['', Validators.required],
      recommandations: ['', Validators.required],
      compteRendu: ['']
    });
  }

  loadAcceptedAppointments(): void {
    this.loading = true;
    this.doctorService.getAppointments().subscribe({
      next: (appointments) => {
        console.log('=== DEBUG: All appointments:', appointments);
        this.acceptedAppointments = appointments.filter(apt => apt.status === 'ACCEPTED');
        console.log('=== DEBUG: Accepted appointments:', this.acceptedAppointments);
        this.loading = false;
      },
      error: (err) => {
        console.error('=== ERROR loading appointments:', err);
        this.loading = false;
        this.showAlert('Error loading consultations', 'error');
      }
    });
  }

  openRecordModal(appointment: Appointment): void {
    this.selectedAppointment = appointment;
    this.recordForm.reset();
    this.showRecordModal = true;
  }

  closeRecordModal(): void {
    this.showRecordModal = false;
    this.selectedAppointment = null;
    this.recordForm.reset();
  }

  saveRecord(): void {
    if (this.recordForm.invalid || !this.selectedAppointment) return;

    const recordData = this.recordForm.value;
    
    this.doctorService.completeAppointment(this.selectedAppointment.id!, recordData).subscribe({
      next: () => {
        this.showAlert('Medical record saved successfully', 'success');
        this.closeRecordModal();
        this.loadAcceptedAppointments();
      },
      error: () => {
        this.showAlert('Error saving record', 'error');
      }
    });
  }

  private showAlert(msg: string, type: 'success' | 'error'): void {
    this.alertMsg = msg;
    this.alertType = type;
    setTimeout(() => this.alertMsg = '', 4000);
  }
}
