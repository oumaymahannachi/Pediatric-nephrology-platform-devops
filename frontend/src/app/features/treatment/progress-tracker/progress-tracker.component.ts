import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProgressService } from '../services/progress.service';

@Component({
  selector: 'app-progress-tracker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './progress-tracker.component.html',
  styleUrls: ['./progress-tracker.component.css']
})
export class ProgressTrackerComponent implements OnInit {
  treatments: any[] = [];
  logs: any[] = [];
  analysis: any = null;
  
  showForm = false;
  selectedTreatmentId = '';
  
  formData = {
    treatmentId: '',
    date: new Date().toISOString().slice(0, 10),
    symptomes: {} as any,
    notes: '',
    poids: null as number | null,
    taille: null as number | null,
    humeur: '',
    niveauEnergie: 5,
    qualiteSommeil: 5
  };
  
  symptomesList = [
    { nom: 'Fatigue', key: 'fatigue' },
    { nom: 'Douleur', key: 'douleur' },
    { nom: 'Nausée', key: 'nausee' },
    { nom: 'Maux de tête', key: 'mauxTete' },
    { nom: 'Essoufflement', key: 'essoufflement' }
  ];

  constructor(private progressService: ProgressService) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    const patientId = localStorage.getItem('userId') || 'patient-test';
    
    this.progressService.getProgressByPatient(patientId).subscribe({
      next: (response: any) => {
        this.logs = response.data || [];
      }
    });
  }

  onTreatmentChange() {
    if (this.selectedTreatmentId) {
      this.progressService.analyzeProgress(this.selectedTreatmentId).subscribe({
        next: (response: any) => {
          this.analysis = response.data || null;
        }
      });
    }
  }

  submitLog() {
    this.progressService.logProgress(this.formData).subscribe({
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
      treatmentId: '',
      date: new Date().toISOString().slice(0, 10),
      symptomes: {},
      notes: '',
      poids: null,
      taille: null,
      humeur: '',
      niveauEnergie: 5,
      qualiteSommeil: 5
    };
  }
}
