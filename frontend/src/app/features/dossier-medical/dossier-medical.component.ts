import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ConsultationListComponent } from './consultation-list/consultation-list.component';
import { DialyseListComponent } from './dialyse-list/dialyse-list.component';
import { ExamenListComponent } from './examen-list/examen-list.component';
import { MedicalIntelligenceComponent } from './medical-intelligence/medical-intelligence.component';
import { ReportService } from '../../core/services/report.service';

interface Child {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-dossier-medical',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    ConsultationListComponent,
    DialyseListComponent,
    ExamenListComponent,
    MedicalIntelligenceComponent
  ],
  templateUrl: './dossier-medical.component.html',
  styleUrls: ['./dossier-medical.component.css']
})
export class DossierMedicalComponent implements OnInit {
  children: Child[] = [];
  selectedChildId: string | null = null;
  selectedChild: Child | null = null;
  activeTab: 'consultations' | 'dialyse' | 'examens' | 'intelligence' = 'consultations';
  loading = false;
  generatingReport = false;

  constructor(
    private http: HttpClient,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    this.loadChildren();
  }

  loadChildren(): void {
    this.loading = true;
    this.http.get<any>(`${environment.apiUrl}/doctor/patients`).subscribe({
      next: (response) => {
        this.children = response.map((c: any) => ({
          id: c.id,
          fullName: c.fullName,
          dateOfBirth: c.dateOfBirth,
          gender: c.gender
        }));
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading children:', err);
        this.loading = false;
      }
    });
  }

  onChildChange(): void {
    if (this.selectedChildId) {
      this.selectedChild = this.children.find(c => c.id === this.selectedChildId) || null;
    } else {
      this.selectedChild = null;
    }
  }

  setTab(tab: 'consultations' | 'dialyse' | 'examens' | 'intelligence'): void {
    this.activeTab = tab;
  }

  calculateAge(dateOfBirth: string): string {
    const today = new Date();
    const birthDate = new Date(dateOfBirth);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    
    return `${age} ans`;
  }

  generatePDFReport(): void {
    if (!this.selectedChild || !this.selectedChildId) {
      return;
    }

    this.generatingReport = true;
    
    this.reportService.generatePDFReport(this.selectedChildId, this.selectedChild.fullName).subscribe({
      next: (blob) => {
        const filename = `medical_report_${this.selectedChild!.fullName.replace(/\s+/g, '_')}_${new Date().toISOString().split('T')[0]}.pdf`;
        this.reportService.downloadPDF(blob, filename);
        this.generatingReport = false;
        alert('PDF report generated successfully!');
      },
      error: (err) => {
        console.error('Error generating PDF:', err);
        this.generatingReport = false;
        alert('Error generating PDF report. Please try again.');
      }
    });
  }
}
