import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NgApexchartsModule, ApexOptions } from 'ng-apexcharts';

import { LabResultService } from '../services/lab-result.service';
import { PdfExportService } from '../services/pdf-export.service';

import { LabResult, TestType, ResultStatus, CKDStage } from '../models/lab-result.model';
import { environment } from '../../../../environments/environment';

interface Child {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-doctor-lab-results',
  standalone: true,
  imports: [CommonModule, FormsModule, NgApexchartsModule],
  templateUrl: './doctor-lab-results.component.html',
  styleUrls: ['./doctor-lab-results.component.css']
})
export class DoctorLabResultsComponent implements OnInit {
  labResults: LabResult[] = [];
  filteredResults: LabResult[] = [];
  children: Child[] = [];
  loading = false;
  loadingChildren = false;
  error: string | null = null;
  selectedChildId: string | null = null;

  // Search and filters
  searchTerm = '';
  filterType: TestType | 'ALL' = 'ALL';
  filterAbnormal: 'ALL' | 'ABNORMAL' | 'NORMAL' = 'ALL';
  sortBy: 'date' | 'eGFR' | 'status' = 'date';
  sortOrder: 'asc' | 'desc' = 'desc';

  // Modal states
  showDetailsModal = false;
  showAddModal = false;
  showEditModal = false;
  selectedResult: LabResult | null = null;

  // Form data
  formData: any = {
    testDate: new Date().toISOString().slice(0, 16),
    testType: TestType.BLOOD,
    laboratoryName: '',
    parentEmail: '',
    parentPhone: '',
    sendEmailNotification: true,
    sendSmsNotification: false
  };

  TestType = TestType;
  ResultStatus = ResultStatus;

  // Chart
  eGFRChartOptions: ApexOptions = {};
  showChart = false;

  constructor(
    private labResultService: LabResultService,
    private http: HttpClient,
    private router: Router,
    private pdfExport: PdfExportService
  ) {}

  ngOnInit(): void {
    this.loadChildren();
  }

  // ------------------ LOAD PATIENTS ------------------
  loadChildren(): void {
    this.loadingChildren = true;
    this.http.get<any>(`${environment.apiUrl}/doctor/patients`).subscribe({
      next: (response) => {
        const arr = Array.isArray(response) ? response : (response?.data ?? []);
        this.children = arr.map((c: any) => ({
          id: c.id,
          fullName: c.fullName,
          dateOfBirth: c.dateOfBirth,
          gender: c.gender
        }));
        this.loadingChildren = false;
      },
      error: (err) => {
        console.error('Error loading patients:', err);
        this.loadingChildren = false;
        this.error = 'Error loading patients';
      }
    });
  }

  onChildChange(): void {
    if (this.selectedChildId) {
      this.loadLabResults(this.selectedChildId);
    } else {
      this.labResults = [];
      this.filteredResults = [];
    }
  }

  // ------------------ LOAD LAB RESULTS ------------------
  loadLabResults(patientId: string): void {
    this.loading = true;
    this.error = null;

    this.labResultService.getLabResultsByPatient(patientId).subscribe({
      next: (response) => {
        const arr = Array.isArray(response) ? response : (response?.data ?? []);
        this.labResults = arr;
        this.applyFilters();
        this.loading = false;
        this.buildEGFRChart();
      },
      error: (err) => {
        console.error(err);
        this.error = 'Error loading lab results';
        this.loading = false;
      }
    });
  }

  // ------------------ FILTERS ------------------
  applyFilters(): void {
    let filtered = [...this.labResults];

    // Search
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(r =>
        (r.laboratoryName?.toLowerCase().includes(term) ?? false) ||
        (r.doctorNotes?.toLowerCase().includes(term) ?? false) ||
        (r.alerts?.some(a => a.toLowerCase().includes(term)) ?? false)
      );
    }

    // Type
    if (this.filterType !== 'ALL') {
      filtered = filtered.filter(r => r.testType === this.filterType);
    }

    // Abnormal
    if (this.filterAbnormal === 'ABNORMAL') {
      filtered = filtered.filter(r => r.isAbnormal);
    } else if (this.filterAbnormal === 'NORMAL') {
      filtered = filtered.filter(r => !r.isAbnormal);
    }

    // Sort
    filtered.sort((a, b) => {
      let comparison = 0;

      if (this.sortBy === 'date') {
        comparison = new Date(a.testDate as any).getTime() - new Date(b.testDate as any).getTime();
      } else if (this.sortBy === 'eGFR') {
        comparison = (a.eGFR ?? 0) - (b.eGFR ?? 0);
      } else if (this.sortBy === 'status') {
        comparison = String(a.status ?? '').localeCompare(String(b.status ?? ''));
      }

      return this.sortOrder === 'asc' ? comparison : -comparison;
    });

    this.filteredResults = filtered;
  }

  onSearchChange(): void { this.applyFilters(); }
  onFilterChange(): void { this.applyFilters(); }

  toggleSortOrder(): void {
    this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    this.applyFilters();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

  // ------------------ MODALS ------------------
  openAddModal(): void {
    if (!this.selectedChildId) {
      alert('Please select a patient first');
      return;
    }
    this.formData = {
      patientId: this.selectedChildId,
      testDate: new Date().toISOString().slice(0, 16),
      testType: TestType.BLOOD,
      laboratoryName: '',
      parentEmail: '',
      parentPhone: '',
      sendEmailNotification: true,
      sendSmsNotification: false
    };
    this.showAddModal = true;
  }

  closeAddModal(): void { this.showAddModal = false; }

  submitLabResult(): void {
    this.loading = true;
    this.labResultService.createLabResult(this.formData).subscribe({
      next: () => {
        this.loading = false;
        this.closeAddModal();
        if (this.selectedChildId) this.loadLabResults(this.selectedChildId);
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.error = 'Error creating lab result';
      }
    });
  }

  viewDetails(result: LabResult): void {
    this.selectedResult = result;
    this.showDetailsModal = true;
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedResult = null;
  }

  openEditModal(result: LabResult): void {
    this.selectedResult = result;
    this.formData = {
      patientId: (result as any).patientId,
      testDate: String(result.testDate ?? '').slice(0, 16),
      testType: result.testType,
      creatinine: (result as any).creatinine,
      urea: (result as any).urea,
      sodium: (result as any).sodium,
      potassium: (result as any).potassium,
      calcium: (result as any).calcium,
      phosphorus: (result as any).phosphorus,
      hemoglobin: (result as any).hemoglobin,
      albumin: (result as any).albumin,
      bicarbonate: (result as any).bicarbonate,
      urineProtein: (result as any).urineProtein,
      urineCreatinine: (result as any).urineCreatinine,
      urineAspect: (result as any).urineAspect,
      urineAlbumin: (result as any).urineAlbumin,
      doctorNotes: result.doctorNotes,
      laboratoryName: result.laboratoryName
    };
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedResult = null;
  }

  updateLabResult(): void {
    if (!this.selectedResult) return;

    this.loading = true;
    this.labResultService.updateLabResult((this.selectedResult as any).id, this.formData).subscribe({
      next: () => {
        this.loading = false;
        this.closeEditModal();
        if (this.selectedChildId) this.loadLabResults(this.selectedChildId);
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.error = 'Error updating lab result';
      }
    });
  }

  deleteLabResult(result: LabResult): void {
    if (!confirm('Are you sure you want to delete this lab result?')) return;

    this.loading = true;
    this.labResultService.deleteLabResult((result as any).id).subscribe({
      next: () => {
        this.loading = false;
        if (this.selectedChildId) this.loadLabResults(this.selectedChildId);
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.error = 'Error deleting lab result';
      }
    });
  }

  validateResult(result: LabResult): void {
    this.loading = true;
    this.labResultService.validateLabResult((result as any).id).subscribe({
      next: () => {
        this.loading = false;
        if (this.selectedChildId) this.loadLabResults(this.selectedChildId);
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.error = 'Error validating lab result';
      }
    });
  }

  // ------------------ PDF EXPORT ✅ ------------------
  exportDoctorPDF(result: LabResult): void {
    this.pdfExport.exportLabResult(result, 'doctor');
  }

  exportParentPDF(result: LabResult): void {
    this.pdfExport.exportLabResult(result, 'parent');
  }

  // ------------------ UI HELPERS ------------------
  getStatusClass(status: ResultStatus): string {
    const classes: Record<ResultStatus, string> = {
      [ResultStatus.PENDING]: 'status-pending',
      [ResultStatus.VALIDATED]: 'status-validated',
      [ResultStatus.REVIEWED]: 'status-reviewed'
    };
    return classes[status] || '';
  }

  getStatusLabel(status: ResultStatus): string {
    const labels: Record<ResultStatus, string> = {
      [ResultStatus.PENDING]: 'Pending',
      [ResultStatus.VALIDATED]: 'Validated',
      [ResultStatus.REVIEWED]: 'Reviewed'
    };
    return labels[status] || String(status);
  }

  getCKDStageLabel(stage: CKDStage): string {
    const labels: Record<CKDStage, string> = {
      [CKDStage.STAGE_1]: 'Stage 1 (Normal)',
      [CKDStage.STAGE_2]: 'Stage 2 (Mild)',
      [CKDStage.STAGE_3A]: 'Stage 3A (Moderate)',
      [CKDStage.STAGE_3B]: 'Stage 3B (Moderate-Severe)',
      [CKDStage.STAGE_4]: 'Stage 4 (Severe)',
      [CKDStage.STAGE_5]: 'Stage 5 (Kidney Failure)',
      [CKDStage.UNKNOWN]: 'Unknown'
    };
    return labels[stage] || String(stage);
  }

  getCKDStageClass(stage: CKDStage): string {
    if (stage === CKDStage.STAGE_1 || stage === CKDStage.STAGE_2) return 'ckd-normal';
    if (stage === CKDStage.STAGE_3A || stage === CKDStage.STAGE_3B) return 'ckd-moderate';
    if (stage === CKDStage.STAGE_4) return 'ckd-severe';
    if (stage === CKDStage.STAGE_5) return 'ckd-critical';
    return '';
  }

  buildEGFRChart(): void {
    const allResults = [...this.labResults]
      .sort((a, b) => new Date(a.testDate as any).getTime() - new Date(b.testDate as any).getTime());

    // If no real eGFR data, use demo data to show chart functionality
    const hasRealEGFR = allResults.some(r => r.eGFR != null && r.eGFR > 0);

    let chartData: { date: string, eGFR: number }[];

    if (hasRealEGFR) {
      chartData = allResults.map(r => ({
        date: new Date(r.testDate as any).toLocaleDateString('fr-FR'),
        eGFR: r.eGFR ?? 0
      }));
    } else {
      // Demo data showing CKD progression
      chartData = [
        { date: '01/01/2026', eGFR: 72 },
        { date: '01/02/2026', eGFR: 65 },
        { date: '01/03/2026', eGFR: 58 },
        { date: '01/04/2026', eGFR: 52 },
        { date: '16/04/2026', eGFR: 48 }
      ];
    }

    if (chartData.length === 0) { this.showChart = false; return; }

    this.eGFRChartOptions = {
      series: [{ name: 'eGFR', data: chartData.map(d => d.eGFR) }],
      chart: { type: 'line', height: 300, toolbar: { show: false } },
      stroke: { curve: 'smooth', width: 3 },
      colors: ['#6366f1'],
      xaxis: { categories: chartData.map(d => d.date), title: { text: 'Date' } },
      yaxis: { title: { text: 'eGFR (mL/min/1.73m²)' }, min: 0, max: 120 },
      annotations: {
        yaxis: [
          { y: 60, borderColor: '#f59e0b', label: { text: 'Stage 3 (<60)', style: { color: '#f59e0b' } } },
          { y: 30, borderColor: '#ef4444', label: { text: 'Stage 4 (<30)', style: { color: '#ef4444' } } },
          { y: 15, borderColor: '#7f1d1d', label: { text: 'Stage 5 (<15)', style: { color: '#7f1d1d' } } }
        ]
      },
      markers: { size: 5 },
      tooltip: { y: { formatter: (val: number) => val.toFixed(1) + ' mL/min/1.73m²' } },
      title: { text: hasRealEGFR ? 'eGFR Evolution' : 'eGFR Evolution (Demo)', align: 'left' }
    };
    this.showChart = true;
  }

  goBack(): void {
    this.router.navigate(['/doctor']);
  }
}