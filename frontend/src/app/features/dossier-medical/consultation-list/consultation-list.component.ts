import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConsultationService } from '../services/consultation.service';
import { Consultation } from '../models/consultation.model';

@Component({
  selector: 'app-consultation-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './consultation-list.component.html',
  styleUrls: ['./consultation-list.component.css']
})
export class ConsultationListComponent implements OnInit, OnChanges {
  @Input() patientId!: string;

  consultations: Consultation[] = [];
  loading = false;
  showModal = false;
  consultationForm!: FormGroup;
  editingConsultation: Consultation | null = null;

  constructor(
    private consultationService: ConsultationService,
    private fb: FormBuilder
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    if (this.patientId) {
      this.loadConsultations();
    }
  }

  ngOnChanges(): void {
    if (this.patientId) {
      this.loadConsultations();
    }
  }

  initForm(): void {
    this.consultationForm = this.fb.group({
      dateRendezVous: ['', Validators.required],
      motifConsultation: ['', Validators.required],
      observationsCliniques: [''],
      diagnostic: [''],
      recommandations: [''],
      compteRendu: [''],
      statut: ['PLANIFIEE']
    });
  }

  loadConsultations(): void {
    this.loading = true;
    this.consultationService.getConsultationsByPatient(this.patientId).subscribe({
      next: (response) => {
        this.consultations = response.data || response;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading consultations:', err);
        this.loading = false;
      }
    });
  }

  openModal(consultation?: Consultation): void {
    this.editingConsultation = consultation || null;
    
    if (consultation) {
      this.consultationForm.patchValue(consultation);
    } else {
      this.consultationForm.reset();
      this.consultationForm.patchValue({
        dateRendezVous: new Date().toISOString().slice(0, 16),
        statut: 'PLANIFIEE'
      });
    }
    
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingConsultation = null;
    this.consultationForm.reset();
  }

  saveConsultation(): void {
    if (this.consultationForm.invalid) return;

    // Ce composant n'est plus utilisé - les consultations sont créées par les parents
    // via ParentConsultationRequestComponent
    alert('Les consultations sont maintenant créées par les parents. Utilisez /parent/consultations');
    this.closeModal();
  }

  deleteConsultation(id: string): void {
    if (!confirm('Supprimer cette consultation?')) return;

    this.consultationService.deleteConsultation(id).subscribe({
      next: () => this.loadConsultations(),
      error: (err) => console.error('Error deleting consultation:', err)
    });
  }
}
