import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PrescriptionService } from '../services/prescription.service';
import { Prescription, StatutPrescription } from '../models/prescription.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface Patient {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-prescription-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './prescription-list.component.html',
  styleUrls: ['./prescription-list.component.css']
})
export class PrescriptionListComponent implements OnInit {
  prescriptions: Prescription[] = [];
  filteredPrescriptions: Prescription[] = [];
  patients: Patient[] = [];
  loading = false;
  error: string | null = null;
  selectedPatientId: string | null = null;
  
  // Search and sort
  searchTerm = '';
  sortBy: 'date' | 'status' | 'diagnostic' = 'date';
  sortOrder: 'asc' | 'desc' = 'desc';

  constructor(
    private prescriptionService: PrescriptionService,
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
      this.loadPrescriptionsByPatient(this.selectedPatientId);
    } else {
      this.prescriptions = [];
      this.filteredPrescriptions = [];
    }
  }

  loadPrescriptionsByPatient(patientId: string): void {
    this.loading = true;
    this.error = null;
    
    this.prescriptionService.getPrescriptionsByPatient(patientId).subscribe({
      next: (response) => {
        this.prescriptions = response.data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading prescriptions';
        this.loading = false;
        console.error(err);
      }
    });
  }
  
  applyFilters(): void {
    let filtered = [...this.prescriptions];
    
    // Search filter
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(p => 
        p.diagnostic.toLowerCase().includes(term) ||
        p.notes?.toLowerCase().includes(term) ||
        p.medicaments.some(m => 
          m.nomCommercial.toLowerCase().includes(term) ||
          m.dci.toLowerCase().includes(term)
        )
      );
    }
    
    // Sort
    filtered.sort((a, b) => {
      let comparison = 0;
      
      if (this.sortBy === 'date') {
        comparison = new Date(a.datePrescription).getTime() - new Date(b.datePrescription).getTime();
      } else if (this.sortBy === 'status') {
        comparison = a.statut.localeCompare(b.statut);
      } else if (this.sortBy === 'diagnostic') {
        comparison = a.diagnostic.localeCompare(b.diagnostic);
      }
      
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });
    
    this.filteredPrescriptions = filtered;
  }
  
  onSearchChange(): void {
    this.applyFilters();
  }
  
  onSortChange(): void {
    this.applyFilters();
  }
  
  toggleSortOrder(): void {
    this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    this.applyFilters();
  }
  
  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

  addPrescription(): void {
    this.router.navigate(['/doctor/prescriptions/new'], {
      state: { patientId: this.selectedPatientId }
    });
  }

  editPrescription(prescription: Prescription): void {
    this.router.navigate(['/doctor/prescriptions/edit', prescription.id], {
      state: { prescription: prescription }
    });
  }

  deletePrescription(prescription: Prescription): void {
    if (confirm('Are you sure you want to delete this prescription?')) {
      this.prescriptionService.deletePrescription(prescription.id).subscribe({
        next: () => {
          alert('Prescription deleted successfully');
          if (this.selectedPatientId) {
            this.loadPrescriptionsByPatient(this.selectedPatientId);
          }
        },
        error: (err) => {
          alert('Error deleting prescription');
          console.error(err);
        }
      });
    }
  }

  getStatutClass(statut: StatutPrescription): string {
    const classes: Record<StatutPrescription, string> = {
      [StatutPrescription.ACTIVE]: 'status-active',
      [StatutPrescription.EXPIREE]: 'status-expired',
      [StatutPrescription.TERMINEE]: 'status-completed',
      [StatutPrescription.ANNULEE]: 'status-cancelled',
      [StatutPrescription.RENOUVELEE]: 'status-renewed'
    };
    return classes[statut] || '';
  }

  getStatutLabel(statut: StatutPrescription): string {
    const labels: Record<StatutPrescription, string> = {
      [StatutPrescription.ACTIVE]: 'Active',
      [StatutPrescription.EXPIREE]: 'Expired',
      [StatutPrescription.TERMINEE]: 'Completed',
      [StatutPrescription.ANNULEE]: 'Cancelled',
      [StatutPrescription.RENOUVELEE]: 'Renewed'
    };
    return labels[statut] || statut;
  }
}
