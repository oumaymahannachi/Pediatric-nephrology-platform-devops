import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { DialysisService } from '../../../../core/services/dialysis.service';
import { DialysisPrescription } from '../../../../core/models/dialysis.model';
import { ExaminationService } from '../../../../core/services/examination.service';
import { BloodTest, LabResult, MedicalImaging } from '../../../../core/models/examination.model';

interface Child {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-medical-monitoring',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './medical-monitoring.component.html',
  styleUrls: ['./medical-monitoring.component.css']
})
export class MedicalMonitoringComponent implements OnInit {
  children: Child[] = [];
  selectedChildId: string | null = null;
  selectedChild: Child | null = null;
  activeTab: 'dialysis' | 'labs' | 'imaging' | 'history' = 'dialysis';
  loading = false;

  // Dialysis data
  dialysisPrescriptions: DialysisPrescription[] = [];

  // Examination data
  bloodTests: BloodTest[] = [];
  labResults: LabResult[] = [];
  medicalImagings: MedicalImaging[] = [];
  allHistory: any[] = [];
  filteredHistory: any[] = [];
  historyFilter: string = 'all';
  dateRange: string = 'all';

  constructor(
    private http: HttpClient,
    private dialysisService: DialysisService,
    private examinationService: ExaminationService
  ) {}

  ngOnInit(): void {
    this.loadChildren();
  }

  loadChildren(): void {
    this.loading = true;
    this.http.get<any>(`${environment.apiUrl}/parent/children`).subscribe({
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
      this.loadData();
    } else {
      this.selectedChild = null;
    }
  }

  loadData(): void {
    if (!this.selectedChildId) return;
    
    if (this.activeTab === 'dialysis') {
      this.loadDialysisData();
    } else if (this.activeTab === 'labs') {
      this.loadLabResults();
    } else if (this.activeTab === 'imaging') {
      this.loadMedicalImaging();
    } else if (this.activeTab === 'history') {
      this.loadHistory();
    }
  }

  loadDialysisData(): void {
    if (!this.selectedChildId) return;
    
    this.dialysisService.getPrescriptionsByPatient(this.selectedChildId).subscribe({
      next: (prescriptions: DialysisPrescription[]) => {
        this.dialysisPrescriptions = prescriptions;
      },
      error: (err: any) => console.error('Error loading dialysis data:', err)
    });
  }

  setTab(tab: 'dialysis' | 'labs' | 'imaging' | 'history'): void {
    this.activeTab = tab;
    this.loadData();
  }

  calculateAge(dateOfBirth: string): string {
    const today = new Date();
    const birthDate = new Date(dateOfBirth);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    
    return `${age} years`;
  }

  loadLabResults(): void {
    if (!this.selectedChildId) return;
    this.loading = true;
    
    Promise.all([
      this.examinationService.getBloodTestsByPatient(this.selectedChildId).toPromise(),
      this.examinationService.getLabResultsByPatient(this.selectedChildId).toPromise()
    ]).then(([bloodTests, labResults]) => {
      this.bloodTests = bloodTests || [];
      this.labResults = labResults || [];
      this.loading = false;
    }).catch(err => {
      console.error('Error loading lab results:', err);
      this.loading = false;
    });
  }

  loadMedicalImaging(): void {
    if (!this.selectedChildId) return;
    this.loading = true;
    
    this.examinationService.getMedicalImagingByPatient(this.selectedChildId).subscribe({
      next: (imagings) => {
        this.medicalImagings = imagings;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading medical imaging:', err);
        this.loading = false;
      }
    });
  }

  loadHistory(): void {
    if (!this.selectedChildId) return;
    this.loading = true;
    
    Promise.all([
      this.examinationService.getBloodTestsByPatient(this.selectedChildId).toPromise(),
      this.examinationService.getLabResultsByPatient(this.selectedChildId).toPromise(),
      this.examinationService.getMedicalImagingByPatient(this.selectedChildId).toPromise()
    ]).then(([bloodTests, labResults, imagings]) => {
      const history: any[] = [];

      bloodTests?.forEach(test => {
        history.push({
          type: 'blood',
          title: `Blood Test - ${test.testType}`,
          date: test.testDate,
          abnormal: test.abnormal,
          details: test
        });
      });

      labResults?.forEach(result => {
        history.push({
          type: 'lab',
          title: `Lab Result - ${result.testName}`,
          date: result.testDate,
          abnormal: result.abnormal,
          details: result
        });
      });

      imagings?.forEach(imaging => {
        history.push({
          type: 'imaging',
          title: `${imaging.imagingType} - ${imaging.bodyPart}`,
          date: imaging.imagingDate,
          abnormal: imaging.abnormal,
          details: imaging
        });
      });

      history.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
      this.allHistory = history;
      this.filterHistory();
      this.loading = false;
    }).catch(err => {
      console.error('Error loading history:', err);
      this.loading = false;
    });
  }

  filterHistory(): void {
    let filtered = [...this.allHistory];

    if (this.historyFilter !== 'all') {
      filtered = filtered.filter(item => item.type === this.historyFilter);
    }

    if (this.dateRange !== 'all') {
      const now = new Date();
      now.setHours(23, 59, 59, 999);
      let cutoffDate = new Date();

      switch (this.dateRange) {
        case 'week':
          cutoffDate.setDate(now.getDate() - 7);
          break;
        case 'month':
          cutoffDate.setMonth(now.getMonth() - 1);
          break;
        case '3months':
          cutoffDate.setMonth(now.getMonth() - 3);
          break;
        case '6months':
          cutoffDate.setMonth(now.getMonth() - 6);
          break;
        case 'year':
          cutoffDate.setFullYear(now.getFullYear() - 1);
          break;
      }

      cutoffDate.setHours(0, 0, 0, 0);
      filtered = filtered.filter(item => {
        const itemDate = new Date(item.date);
        return itemDate >= cutoffDate && itemDate <= now;
      });
    }

    this.filteredHistory = filtered;
  }

  getCountByType(type: string): number {
    return this.filteredHistory.filter(item => item.type === type).length;
  }

  getAbnormalCount(): number {
    return this.filteredHistory.filter(item => item.abnormal).length;
  }

  hasAttachments(imaging: MedicalImaging): boolean {
    const hasImages = imaging.imageUrls && Array.isArray(imaging.imageUrls) && imaging.imageUrls.length > 0;
    const hasDocs = imaging.documentUrls && Array.isArray(imaging.documentUrls) && imaging.documentUrls.length > 0;
    return !!(hasImages || hasDocs);
  }

  isImageFile(url: string): boolean {
    if (!url) return false;
    return url.startsWith('data:image/') || 
           url.match(/\.(jpg|jpeg|png|gif|bmp|webp)$/i) !== null;
  }

  getFileName(url: string): string {
    if (url.startsWith('data:')) {
      const match = url.match(/data:([^;]+);/);
      if (match) {
        const mimeType = match[1];
        if (mimeType.startsWith('image/')) return 'Image';
        if (mimeType === 'application/pdf') return 'PDF Document';
        return 'File';
      }
      return 'File';
    }
    return url.split('/').pop() || 'File';
  }
}
