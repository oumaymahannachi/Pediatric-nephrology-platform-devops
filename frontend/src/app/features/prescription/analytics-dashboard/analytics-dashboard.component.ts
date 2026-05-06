import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AnalyticsService } from '../services/analytics.service';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './analytics-dashboard.component.html',
  styleUrls: ['./analytics-dashboard.component.css']
})
export class AnalyticsDashboardComponent implements OnInit {
  medecinStats: any = null;
  topMedicaments: any = null;
  interactions: any = null;
  
  selectedPeriod = 30;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit() {
    this.loadAnalytics();
  }

  loadAnalytics() {
    const medecinId = localStorage.getItem('userId') || 'doctor-test';
    
    this.analyticsService.getStatistiquesMedecin(medecinId, this.selectedPeriod).subscribe({
      next: (response: any) => {
        this.medecinStats = response.data || null;
      }
    });
    
    this.analyticsService.getTopMedicaments(this.selectedPeriod).subscribe({
      next: (response: any) => {
        this.topMedicaments = response.data || null;
      }
    });
    
    this.analyticsService.getInteractionsFrequentes().subscribe({
      next: (response: any) => {
        this.interactions = response.data || null;
      }
    });
  }

  onPeriodChange() {
    this.loadAnalytics();
  }

  getStatutKeys(): string[] {
    return this.medecinStats?.repartitionStatut 
      ? Object.keys(this.medecinStats.repartitionStatut) 
      : [];
  }
}
