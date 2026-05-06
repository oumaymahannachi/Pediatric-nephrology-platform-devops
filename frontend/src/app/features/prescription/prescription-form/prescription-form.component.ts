import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { PrescriptionService } from '../services/prescription.service';
import { PrescriptionCreateRequest } from '../models/prescription.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface Patient {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-prescription-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './prescription-form.component.html',
  styleUrls: ['./prescription-form.component.css']
})
export class PrescriptionFormComponent implements OnInit {
  prescriptionForm!: FormGroup;
  loading = false;
  error: string | null = null;
  patientId: string | null = null;
  patients: Patient[] = [];
  loadingPatients = false;
  isEditMode = false;
  prescriptionId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private prescriptionService: PrescriptionService,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.prescriptionId = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!this.prescriptionId;

    console.log('Prescription ID from route:', this.prescriptionId);
    console.log('Is Edit Mode:', this.isEditMode);

    const navigation = this.router.getCurrentNavigation();
    this.patientId = navigation?.extras?.state?.['patientId'];

    this.loadPatients();
    this.initForm();

    if (this.isEditMode && this.prescriptionId) {
      console.log('Loading prescription with ID:', this.prescriptionId);
      this.loadPrescription(this.prescriptionId);
    }
  }

  loadPatients(): void {
    this.loadingPatients = true;
    this.http.get<any>(`${environment.apiUrl}/doctor/patients`).subscribe({
      next: (response) => {
        this.patients = response.map((p: any) => ({
          id: p.id,
          fullName: p.fullName,
          dateOfBirth: p.dateOfBirth,
          gender: p.gender
        }));
        this.loadingPatients = false;
      },
      error: (err) => {
        console.error('Error loading patients:', err);
        this.loadingPatients = false;
      }
    });
  }

  initForm(): void {
    const today = new Date().toISOString().split('T')[0];
    
    this.prescriptionForm = this.fb.group({
      patientId: [this.patientId || '', Validators.required],
      diagnostic: ['', [Validators.required, Validators.maxLength(500)]],
      datePrescription: [today, Validators.required],
      dureeValiditeJours: [30, [Validators.required, Validators.min(1), Validators.max(365)]],
      notes: [''],
      renouvelable: [false],
      nombreRenouvellementsAutorises: [0],
      medicaments: this.fb.array([this.createMedicamentGroup()])
    });
  }

  createMedicamentGroup(): FormGroup {
    return this.fb.group({
      nomCommercial: ['', Validators.required],
      dci: ['', Validators.required],
      formePharmaceutique: [''],
      dosage: [''],
      instructionsSpeciales: [''],
      substitutable: [true],
      posologie: this.fb.group({
        quantite: [1, [Validators.required, Validators.min(0)]],
        unite: ['comprimé(s)', Validators.required],
        frequence: ['', Validators.required],
        momentPrise: [''],
        dureeTraitementJours: [null],
        isPediatrique: [true],
        poidsPatientKg: [null],
        doseParKg: [null],
        ajustementRenal: [false],
        ajustementHepatique: [false],
        justificationAjustement: ['']
      })
    });
  }

  get medicaments(): FormArray {
    return this.prescriptionForm.get('medicaments') as FormArray;
  }

  addMedicament(): void {
    this.medicaments.push(this.createMedicamentGroup());
  }

  removeMedicament(index: number): void {
    if (this.medicaments.length > 1) {
      this.medicaments.removeAt(index);
    }
  }

  onSubmit(): void {
    if (this.prescriptionForm.valid) {
      this.loading = true;
      this.error = null;

      const request: PrescriptionCreateRequest = this.prescriptionForm.value;

      const operation = this.isEditMode && this.prescriptionId
        ? this.prescriptionService.updatePrescription(this.prescriptionId, request)
        : this.prescriptionService.createPrescription(request);

      operation.subscribe({
        next: () => {
          alert(this.isEditMode ? 'Prescription modifiée avec succès' : 'Prescription créée avec succès');
          this.router.navigate(['/doctor/prescriptions'], {
            state: { patientId: this.prescriptionForm.value.patientId }
          });
        },
        error: (err) => {
          this.error = this.isEditMode ? 'Erreur lors de la modification de la prescription' : 'Erreur lors de la création de la prescription';
          this.loading = false;
          console.error(err);
        }
      });
    } else {
      this.markFormGroupTouched(this.prescriptionForm);
    }
  }

  loadPrescription(id: string): void {
    console.log('loadPrescription called with ID:', id);
    this.loading = true;
    this.prescriptionService.getPrescription(id).subscribe({
      next: (response) => {
        console.log('Prescription loaded successfully:', response);
        const prescription = response.data;
        this.prescriptionForm.patchValue({
          patientId: prescription.patientId,
          diagnostic: prescription.diagnostic,
          datePrescription: prescription.datePrescription,
          dureeValiditeJours: prescription.dureeValiditeJours,
          notes: prescription.notes,
          renouvelable: prescription.renouvelable,
          nombreRenouvellementsAutorises: prescription.nombreRenouvellementsRestants || 0
        });

        this.medicaments.clear();
        prescription.medicaments.forEach((med: any) => {
          const medGroup = this.createMedicamentGroup();
          medGroup.patchValue({
            nomCommercial: med.nomCommercial,
            dci: med.dci,
            formePharmaceutique: med.formePharmaceutique,
            dosage: med.dosage,
            instructionsSpeciales: med.instructionsSpeciales,
            substitutable: med.substitutable,
            posologie: {
              quantite: med.posologie.quantite,
              unite: med.posologie.unite,
              frequence: med.posologie.frequence,
              momentPrise: med.posologie.momentPrise,
              dureeTraitementJours: med.posologie.dureeTraitementJours,
              isPediatrique: med.posologie.isPediatrique,
              poidsPatientKg: med.posologie.poidsPatientKg,
              doseParKg: med.posologie.doseParKg,
              ajustementRenal: med.posologie.ajustementRenal,
              ajustementHepatique: med.posologie.ajustementHepatique,
              justificationAjustement: med.posologie.justificationAjustement
            }
          });
          this.medicaments.push(medGroup);
        });

        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading prescription:', err);
        console.error('Error details:', JSON.stringify(err, null, 2));
        this.error = 'Error loading prescription: ' + (err.error?.message || err.message || 'Unknown error');
        this.loading = false;
      }
    });
  }

  markFormGroupTouched(formGroup: FormGroup | FormArray): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup || control instanceof FormArray) {
        this.markFormGroupTouched(control);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/doctor/prescriptions']);
  }
}
