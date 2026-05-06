import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdherenceService, AdherenceLog, AdherenceStats } from '../services/adherence.service';
import { PrescriptionService } from '../services/prescription.service';

@Component({
  selector: 'app-adherence-tracker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './adherence-tracker.component.html',
  styleUrls: ['./adherence-tracker.component.css']
})
export class AdherenceTrackerComponent implements OnInit {
  logs: AdherenceLog[] = [];
  stats: AdherenceStats | null = null;
  prescriptions: any[] = [];
  
  showForm = false;
  formData = {
    prescriptionId: '',
    medicamentNom: '',
    datePrise: new Date().toISOString().slice(0, 16),
    prise: true,
    raison: '',
    notes: '',
    effetsSecondaires: ''
  };

  constructor(
    private adherenceService: AdherenceService,
    private prescriptionService: PrescriptionService
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    const patientId = localStorage.getItem('userId') || 'patient-test';
    
    this.adherenceService.getLogsPatient(patientId).subscribe({
      next: (response: any) => {
        this.logs = response.data || [];
      }
    });
    
    this.adherenceService.getStatistiques(patientId).subscribe({
      next: (response: any) => {
        this.stats = response.data || null;
      }
    });
    
    this.prescriptionService.getMyPrescriptions().subscribe({
      next: (response: any) => {
        this.prescriptions = (response.data || []).filter((p: any) => p.statut === 'ACTIVE');
      }
    });
  }

  onPrescriptionChange() {
    const prescription = this.prescriptions.find(p => p.id === this.formData.prescriptionId);
    if (prescription && prescription.medicaments.length > 0) {
      this.formData.medicamentNom = prescription.medicaments[0].nomCommercial;
    }
  }

  submitLog() {
    this.adherenceService.logPriseMedicament(this.formData).subscribe({
      next: () => {
        this.showForm = false;
        this.resetForm();
        this.loadData();
      },
      error: (err) => console.error('Erreur:', err)
    });
  }

  resetForm() {
    this.formData = {
      prescriptionId: '',
      medicamentNom: '',
      datePrise: new Date().toISOString().slice(0, 16),
      prise: true,
      raison: '',
      notes: '',
      effetsSecondaires: ''
    };
  }

  getStatutClass(niveau: string): string {
    const classes: any = {
      'EXCELLENT': 'success',
      'BON': 'info',
      'MOYEN': 'warning',
      'FAIBLE': 'danger'
    };
    return classes[niveau] || 'secondary';
  }
}
