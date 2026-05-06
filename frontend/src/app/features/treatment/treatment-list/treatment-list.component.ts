import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TreatmentService } from '../services/treatment.service';
import { Treatment, StatutTraitement } from '../models/treatment.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface Patient {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-treatment-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './treatment-list.component.html',
  styleUrls: ['./treatment-list.component.css']
})
export class TreatmentListComponent implements OnInit {
  treatments: Treatment[] = [];
  patients: Patient[] = [];
  loading = false;
  error: string | null = null;
  selectedPatientId: string | null = null;

  constructor(
    private treatmentService: TreatmentService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.http.get<any>(`${environment.apiUrl}/doctor/patients`).subscribe({
      next: (response) => {
        this.patients = response.map((p: any) => ({
          id: p.id,
          fullName: p.fullName,
          dateOfBirth: p.dateOfBirth,
          gender: p.gender
        }));
      },
      error: (err) => console.error('Error loading patients:', err)
    });
  }

  onPatientChange(): void {
    if (this.selectedPatientId) {
      this.loadTreatmentsByPatient(this.selectedPatientId);
    } else {
      this.treatments = [];
    }
  }

  loadTreatmentsByPatient(patientId: string): void {
    this.loading = true;
    this.error = null;
    
    this.treatmentService.getTreatmentsByPatient(patientId).subscribe({
      next: (response) => {
        this.treatments = response.data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading treatments';
        this.loading = false;
        console.error(err);
      }
    });
  }

  addTreatment(): void {
    this.router.navigate(['/doctor/treatments/new'], {
      state: { patientId: this.selectedPatientId }
    });
  }

  editTreatment(treatment: Treatment): void {
    this.router.navigate(['/doctor/treatments/edit', treatment.id], {
      state: { treatment: treatment }
    });
  }

  deleteTreatment(treatment: Treatment): void {
    if (confirm('Are you sure you want to delete this treatment?')) {
      this.treatmentService.deleteTreatment(treatment.id).subscribe({
        next: () => {
          alert('Treatment deleted successfully');
          if (this.selectedPatientId) {
            this.loadTreatmentsByPatient(this.selectedPatientId);
          }
        },
        error: (err) => {
          alert('Error deleting treatment');
          console.error(err);
        }
      });
    }
  }

  getStatutClass(statut: StatutTraitement): string {
    const classes: Record<StatutTraitement, string> = {
      [StatutTraitement.EN_COURS]: 'status-active',
      [StatutTraitement.TERMINE]: 'status-completed',
      [StatutTraitement.SUSPENDU]: 'status-suspended',
      [StatutTraitement.ANNULE]: 'status-cancelled'
    };
    return classes[statut] || '';
  }

  getStatutLabel(statut: StatutTraitement): string {
    const labels: Record<StatutTraitement, string> = {
      [StatutTraitement.EN_COURS]: 'Active',
      [StatutTraitement.TERMINE]: 'Completed',
      [StatutTraitement.SUSPENDU]: 'Suspended',
      [StatutTraitement.ANNULE]: 'Cancelled'
    };
    return labels[statut] || statut;
  }
}
