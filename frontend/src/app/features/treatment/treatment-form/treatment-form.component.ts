import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { TreatmentService } from '../services/treatment.service';
import { TreatmentCreateRequest } from '../models/treatment.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface Patient {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-treatment-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './treatment-form.component.html',
  styleUrls: ['./treatment-form.component.css']
})
export class TreatmentFormComponent implements OnInit {
  treatmentForm!: FormGroup;
  loading = false;
  error: string | null = null;
  patients: Patient[] = [];
  isEditMode = false;
  treatmentId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private treatmentService: TreatmentService,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.treatmentId = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!this.treatmentId;

    this.loadPatients();
    this.initForm();

    if (this.isEditMode && this.treatmentId) {
      this.loadTreatment(this.treatmentId);
    }
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

  initForm(): void {
    const today = new Date().toISOString().split('T')[0];
    
    this.treatmentForm = this.fb.group({
      patientId: ['', Validators.required],
      diagnostic: ['', Validators.required],
      dateDebut: [today, Validators.required],
      dateFin: ['', Validators.required],
      objectifTraitement: [''],
      notes: [''],
      medicaments: this.fb.array([this.createMedicamentGroup()]),
      recommandations: this.fb.array([])
    });
  }

  createMedicamentGroup(): FormGroup {
    return this.fb.group({
      nomCommercial: ['', Validators.required],
      dci: ['', Validators.required],
      formePharmaceutique: [''],
      dosage: [''],
      instructionsSpeciales: [''],
      posologie: this.fb.group({
        quantite: [1, [Validators.required, Validators.min(0)]],
        unite: ['comprimé(s)', Validators.required],
        frequence: ['', Validators.required],
        momentPrise: [''],
        dureeTraitementJours: [null]
      })
    });
  }

  get medicaments(): FormArray {
    return this.treatmentForm.get('medicaments') as FormArray;
  }

  get recommandations(): FormArray {
    return this.treatmentForm.get('recommandations') as FormArray;
  }

  addMedicament(): void {
    this.medicaments.push(this.createMedicamentGroup());
  }

  removeMedicament(index: number): void {
    if (this.medicaments.length > 1) {
      this.medicaments.removeAt(index);
    }
  }

  addRecommandation(): void {
    this.recommandations.push(this.fb.control('', Validators.required));
  }

  removeRecommandation(index: number): void {
    this.recommandations.removeAt(index);
  }

  loadTreatment(id: string): void {
    this.loading = true;
    this.treatmentService.getTreatment(id).subscribe({
      next: (response) => {
        const treatment = response.data;
        this.treatmentForm.patchValue({
          patientId: treatment.patientId,
          diagnostic: treatment.diagnostic,
          dateDebut: treatment.dateDebut,
          dateFin: treatment.dateFin,
          objectifTraitement: treatment.objectifTraitement,
          notes: treatment.notes
        });

        this.medicaments.clear();
        treatment.medicaments.forEach((med: any) => {
          const medGroup = this.createMedicamentGroup();
          medGroup.patchValue(med);
          this.medicaments.push(medGroup);
        });

        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading treatment';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    if (this.treatmentForm.valid) {
      this.loading = true;
      this.error = null;

      const formValue = this.treatmentForm.value;
      const request: TreatmentCreateRequest = {
        ...formValue,
        recommandations: formValue.recommandations.filter((r: string) => r && r.trim() !== '')
      };

      const operation = this.isEditMode && this.treatmentId
        ? this.treatmentService.updateTreatment(this.treatmentId, request)
        : this.treatmentService.createTreatment(request);

      operation.subscribe({
        next: () => {
          alert(this.isEditMode ? 'Treatment updated successfully' : 'Treatment created successfully');
          this.router.navigate(['/doctor/treatments']);
        },
        error: (err) => {
          console.error('Complete error:', err);
          this.error = err.error?.message || 'Error saving treatment';
          this.loading = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/doctor/treatments']);
  }
}
