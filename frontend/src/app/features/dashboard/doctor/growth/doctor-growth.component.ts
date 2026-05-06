import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { DoctorService } from '../../../../core/services/doctor.service';
import { Child } from '../../../../core/models/child.model';
import { GrowthMeasurement, DietaryRestriction, NutritionalPlan } from '../../../../core/models/treatment.model';

@Component({
  selector: 'app-doctor-growth',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './doctor-growth.component.html',
  styleUrl: './doctor-growth.component.scss'
})
export class DoctorGrowthComponent implements OnInit {
  loading = true;
  patients: Child[] = [];
  selectedPatientId = '';
  growthSubTab: 'measurements' | 'restrictions' | 'plans' = 'measurements';

  patientMeasurements: GrowthMeasurement[] = [];
  patientRestrictions: DietaryRestriction[] = [];
  patientPlans: NutritionalPlan[] = [];

  constructor(private doctorService: DoctorService) {}

  ngOnInit(): void {
    this.doctorService.getPatients().subscribe({
      next: (p) => {
        this.patients = p;
        if (p.length > 0) {
          this.selectedPatientId = p[0].id!;
          this.loadPatientGrowth();
        }
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onPatientSelected(): void { this.loadPatientGrowth(); }

  loadPatientGrowth(): void {
    if (!this.selectedPatientId) return;
    this.doctorService.getPatientMeasurements(this.selectedPatientId).subscribe({
      next: (m) => this.patientMeasurements = m
    });
    this.doctorService.getPatientRestrictions(this.selectedPatientId).subscribe({
      next: (r) => this.patientRestrictions = r
    });
    this.doctorService.getPatientPlans(this.selectedPatientId).subscribe({
      next: (p) => this.patientPlans = p
    });
  }
}
