import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { DoctorService } from '../../../../core/services/doctor.service';
import { Child } from '../../../../core/models/child.model';
import { NutritionalPlan } from '../../../../core/models/treatment.model';

@Component({
  selector: 'app-doctor-nutrition',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './doctor-nutrition.component.html',
  styleUrl: './doctor-nutrition.component.scss'
})
export class DoctorNutritionComponent implements OnInit {
  loading = true;
  patients: Child[] = [];
  nutritionalPlans: NutritionalPlan[] = [];

  view: 'list' | 'form' = 'list';
  editingPlan: NutritionalPlan | null = null;
  planForm!: FormGroup;
  planSaving = false;

  alertMsg = '';
  alertType: 'success' | 'error' = 'success';

  constructor(private doctorService: DoctorService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.planForm = this.fb.group({
      childId: ['', Validators.required],
      title: ['', Validators.required],
      description: [''],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      goals: [''],
      restrictions: [''],
      meals: this.fb.array([])
    });

    this.doctorService.getPatients().subscribe({ next: (p) => this.patients = p });
    this.loadNutritionalPlans();
  }

  get mealsArray(): FormArray { return this.planForm.get('meals') as FormArray; }

  addMealRow(): void {
    this.mealsArray.push(this.fb.group({
      name: ['', Validators.required],
      time: [''],
      description: [''],
      calories: [''],
      notes: ['']
    }));
  }

  removeMealRow(i: number): void { this.mealsArray.removeAt(i); }

  loadNutritionalPlans(): void {
    this.loading = true;
    this.doctorService.getNutritionalPlans().subscribe({
      next: (p) => { this.nutritionalPlans = p; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  openAddPlan(): void {
    this.editingPlan = null;
    this.planForm.reset();
    this.mealsArray.clear();
    this.view = 'form';
  }

  openEditPlan(plan: NutritionalPlan): void {
    this.editingPlan = plan;
    this.planForm.patchValue({
      childId: plan.childId,
      title: plan.title,
      description: plan.description || '',
      startDate: plan.startDate,
      endDate: plan.endDate,
      goals: plan.goals || '',
      restrictions: plan.restrictions || ''
    });
    this.mealsArray.clear();
    if (plan.meals) {
      plan.meals.forEach(meal => {
        this.mealsArray.push(this.fb.group({
          name: [meal.name, Validators.required],
          time: [meal.time || ''],
          description: [meal.description || ''],
          calories: [meal.calories || ''],
          notes: [meal.notes || '']
        }));
      });
    }
    this.view = 'form';
  }

  cancelForm(): void {
    this.view = 'list';
    this.editingPlan = null;
    this.planForm.reset();
    this.mealsArray.clear();
  }

  savePlan(): void {
    if (this.planForm.invalid) return;
    this.planSaving = true;
    const data = this.planForm.value;
    const obs = this.editingPlan
      ? this.doctorService.updateNutritionalPlan(this.editingPlan.id!, data)
      : this.doctorService.createNutritionalPlan(data);
    obs.subscribe({
      next: () => {
        this.view = 'list';
        this.planSaving = false;
        this.showAlert(this.editingPlan ? 'Plan updated' : 'Plan created', 'success');
        this.loadNutritionalPlans();
      },
      error: () => { this.planSaving = false; this.showAlert('Failed to save plan', 'error'); }
    });
  }

  deletePlan(plan: NutritionalPlan): void {
    if (!confirm('Delete this nutritional plan?')) return;
    this.doctorService.deleteNutritionalPlan(plan.id!).subscribe({
      next: () => { this.showAlert('Plan deleted', 'success'); this.loadNutritionalPlans(); },
      error: () => this.showAlert('Failed to delete plan', 'error')
    });
  }

  getPatientName(childId: string): string {
    const p = this.patients.find(c => c.id === childId);
    return p ? p.fullName : childId;
  }

  private showAlert(msg: string, type: 'success' | 'error'): void {
    this.alertMsg = msg;
    this.alertType = type;
    setTimeout(() => this.alertMsg = '', 4000);
  }
}
