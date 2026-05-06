import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { LabResultService } from '../services/lab-result.service';
import { PdfExportService } from '../services/pdf-export.service';

import { LabResult, TestType, CKDStage } from '../models/lab-result.model';
import { environment } from '../../../../environments/environment';
import { ChatModalComponent } from '../../messaging/components/chat-modal/chat-modal.component';
import { SenderType } from '../../messaging/models/message.model';
import { ContextType } from '../../messaging/models/conversation.model';

interface Child {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-parent-lab-results',
  standalone: true,
  imports: [CommonModule, FormsModule, ChatModalComponent],
  templateUrl: './parent-lab-results.component.html',
  styleUrls: ['./parent-lab-results.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ParentLabResultsComponent implements OnInit {
  labResults: LabResult[] = [];
  children: Child[] = [];
  loading = false;
  loadingChildren = false;
  error: string | null = null;
  selectedChildId: string | null = null;

  showDetailsModal = false;
  selectedResult: LabResult | null = null;

  TestType = TestType;

  // Chat properties
  showChatModal = false;
  selectedResultForChat: LabResult | null = null;
  currentUserId = localStorage.getItem('userId') || '';
  currentUserType = SenderType.PARENT;
  ContextType = ContextType;

  constructor(
    private labResultService: LabResultService,
    private http: HttpClient,
    private router: Router,
    private pdfExport: PdfExportService
  ) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    this.selectedChildId = navigation?.extras?.state?.['childId'] ?? null;
    this.loadChildren();
  }

  loadChildren(): void {
    this.loadingChildren = true;

    this.http.get<any>(`${environment.apiUrl}/parent/children`).subscribe({
      next: (response) => {
        const arr = Array.isArray(response) ? response : (response?.data ?? []);
        this.children = arr.map((c: any) => ({
          id: c.id,
          fullName: c.fullName,
          dateOfBirth: c.dateOfBirth,
          gender: c.gender
        }));

        this.loadingChildren = false;

        if (!this.selectedChildId && this.children.length > 0) {
          this.selectedChildId = this.children[0].id;
        }

        if (this.selectedChildId) {
          this.loadLabResults(this.selectedChildId);
        }
      },
      error: (err) => {
        console.error(err);
        this.loadingChildren = false;
        this.error = 'Error loading children list';
      }
    });
  }

  onChildChange(): void {
    if (this.selectedChildId) this.loadLabResults(this.selectedChildId);
    else this.labResults = [];
  }

  loadLabResults(childId: string): void {
    this.loading = true;
    this.error = null;

    this.labResultService.getLabResultsByPatient(childId).subscribe({
      next: (response: any) => {
        const arr = Array.isArray(response) ? response : (response?.data ?? []);
        this.labResults = arr;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.error = 'Error loading lab results';
      }
    });
  }

  // ✅ PDF export for parent
  exportParentPDF(result: LabResult): void {
    this.pdfExport.exportLabResult(result, 'parent');
  }

  viewDetails(result: LabResult): void {
    this.selectedResult = result;
    this.showDetailsModal = true;
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedResult = null;
  }

  getResultStatusClass(result: LabResult): string {
    return result.isAbnormal ? 'status-abnormal' : 'status-normal';
  }

  getResultStatusText(result: LabResult): string {
    return result.isAbnormal ? 'Needs Attention' : 'Normal';
  }

  getCKDStageExplanation(stage: CKDStage): string {
    const explanations: Record<CKDStage, string> = {
      [CKDStage.STAGE_1]: 'Kidney function is normal or near normal.',
      [CKDStage.STAGE_2]: 'Kidney function is slightly reduced.',
      [CKDStage.STAGE_3A]: 'Kidney function is moderately reduced.',
      [CKDStage.STAGE_3B]: 'Kidney function is moderately to severely reduced.',
      [CKDStage.STAGE_4]: 'Kidney function is severely reduced.',
      [CKDStage.STAGE_5]: 'Kidney failure. Dialysis or transplant may be needed.',
      [CKDStage.UNKNOWN]: 'Unable to determine kidney function stage.'
    };
    return explanations[stage] || '';
  }

  getSimpleExplanation(result: LabResult): string {
    if (!result.isAbnormal) return 'All test results are within normal range.';
    let explanation = 'Some test results need attention. ';
    if (result.eGFR != null && result.eGFR < 60) explanation += 'Kidney function is reduced. ';
    if ((result as any).potassium != null && (result as any).potassium > 5.5) explanation += 'Potassium is high. ';
    if ((result as any).hemoglobin != null && (result as any).hemoglobin < 10) explanation += 'Hemoglobin is low. ';
    return explanation + 'Please discuss with your doctor.';
  }

  getActionAdvice(result: LabResult): string[] {
    if (!result.isAbnormal) {
      return ['Continue current plan', 'Schedule next check-up', 'Maintain hydration'];
    }
    const advice: string[] = [
      'Contact your doctor',
      'Monitor symptoms',
      'Continue medications as prescribed'
    ];
    return advice;
  }

  goBack(): void {
    this.router.navigate(['/parent']);
  }

  // Chat methods
  openChatAboutResult(result: LabResult): void {
    this.selectedResultForChat = result;
    this.showChatModal = true;
  }

  closeChat(): void {
    this.showChatModal = false;
    this.selectedResultForChat = null;
  }
}
