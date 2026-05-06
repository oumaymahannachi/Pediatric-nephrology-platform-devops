import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../../core/services/admin.service';
import { LucideAngularModule } from 'lucide-angular';
import { GrowthMeasurement, DietaryRestriction, NutritionalPlan } from '../../../../core/models/treatment.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-treatment',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './admin-treatment.component.html',
  styleUrl: './admin-treatment.component.scss'
})
export class AdminTreatmentComponent implements OnInit {
  loading = true;
  treatmentStats = { children: 0, measurements: 0, plans: 0, restrictions: 0, appointments: 0 };
  allChildren: any[] = [];
  allMeasurements: GrowthMeasurement[] = [];
  allRestrictions: DietaryRestriction[] = [];
  allPlans: NutritionalPlan[] = [];
  activeSubTab = 'children';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.loading = true;
    forkJoin({
      dashboard: this.adminService.getTreatmentDashboard(),
      children: this.adminService.getAllChildren(),
      measurements: this.adminService.getAllMeasurements(),
      restrictions: this.adminService.getAllRestrictions(),
      plans: this.adminService.getAllNutritionalPlans()
    }).subscribe({
      next: ({ dashboard, children, measurements, restrictions, plans }) => {
        this.treatmentStats = {
          children: dashboard.childrenCount || 0,
          measurements: dashboard.measurementsCount || 0,
          plans: dashboard.plansCount || 0,
          restrictions: dashboard.restrictionsCount || 0,
          appointments: dashboard.appointmentsCount || 0
        };
        this.allChildren = children;
        this.allMeasurements = measurements;
        this.allRestrictions = restrictions;
        this.allPlans = plans;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }
}
