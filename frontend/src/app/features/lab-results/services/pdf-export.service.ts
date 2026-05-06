import { Injectable } from '@angular/core';
import { LabResult, CKDStage, TestType } from '../models/lab-result.model';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';

@Injectable({ providedIn: 'root' })
export class PdfExportService {
  // Logo en base64 (sera chargé depuis les assets)
  private logoDataUrl: string | null = null;

  constructor() {
    this.loadLogo();
  }

  private loadLogo(): void {
    const img = new Image();
    img.onload = () => {
      const canvas = document.createElement('canvas');
      canvas.width = img.width;
      canvas.height = img.height;
      const ctx = canvas.getContext('2d');
      if (ctx) {
        ctx.drawImage(img, 0, 0);
        this.logoDataUrl = canvas.toDataURL('image/png');
      }
    };
    img.src = 'assets/brand/pedialink-logo.png';
  }

  exportLabResult(result: LabResult, type: 'parent' | 'doctor'): void {
    const doc = new jsPDF({ unit: 'mm', format: 'a4' });

    if (type === 'parent') {
      this.generateParentPDF(doc, result);
    } else {
      this.generateDoctorPDF(doc, result);
    }

    const filename = `lab-result-${(result as any).id ?? 'unknown'}-${type}-${Date.now()}.pdf`;
    doc.save(filename);
  }

  // =========================
  // PARENT PDF (SIMPLE)
  // =========================
  private generateParentPDF(doc: jsPDF, result: LabResult): void {
    let yPos = 20;

    // Logo
    if (this.logoDataUrl) {
      try {
        doc.addImage(this.logoDataUrl, 'PNG', 20, yPos - 5, 30, 15);
        yPos += 12;
      } catch (e) {
        console.warn('Could not add logo to PDF', e);
      }
    }

    // Header
    doc.setFontSize(20);
    doc.setTextColor(102, 126, 234);
    doc.text('PediaLink', 55, yPos);

    yPos += 10;
    doc.setFontSize(14);
    doc.setTextColor(0, 0, 0);
    doc.text('Laboratory Test Results (Parent Summary)', 20, yPos);

    // Date
    yPos += 8;
    doc.setFontSize(10);
    doc.setTextColor(90, 90, 90);
    const dateStr = result.testDate ? new Date(result.testDate as any).toLocaleString() : 'N/A';
    doc.text(`Test Date: ${dateStr}`, 20, yPos);

    yPos += 12;

    // Status Badge
    if (result.isAbnormal) {
      doc.setFillColor(254, 226, 226);
      doc.setTextColor(153, 27, 27);
      doc.roundedRect(20, yPos - 5, 60, 10, 3, 3, 'F');
      doc.setFontSize(11);
      doc.text('Needs Attention', 24, yPos + 2);
    } else {
      doc.setFillColor(209, 250, 229);
      doc.setTextColor(6, 95, 70);
      doc.roundedRect(20, yPos - 5, 35, 10, 3, 3, 'F');
      doc.setFontSize(11);
      doc.text('Normal', 24, yPos + 2);
    }

    yPos += 18;

    // Key Results Table
    doc.setFontSize(13);
    doc.setTextColor(0, 0, 0);
    doc.text('Key Results:', 20, yPos);
    yPos += 6;

    const keyResults: Array<[string, string, string]> = [];

    if (result.eGFR !== null && result.eGFR !== undefined) {
      keyResults.push([
        'Kidney Function (eGFR)',
        `${Number(result.eGFR).toFixed(1)} mL/min/1.73m²`,
        this.getEGFRStatus(Number(result.eGFR))
      ]);
    }

    if (result.creatinine !== null && result.creatinine !== undefined) {
      keyResults.push([
        'Creatinine',
        `${Number(result.creatinine).toFixed(2)} mg/dL`,
        ''
      ]);
    }

    if (result.potassium !== null && result.potassium !== undefined) {
      const k = Number(result.potassium);
      keyResults.push([
        'Potassium',
        `${k.toFixed(1)} mmol/L`,
        k < 3.5 ? 'Low' : (k > 5.5 ? 'High' : '')
      ]);
    }

    if (result.hemoglobin !== null && result.hemoglobin !== undefined) {
      const hb = Number(result.hemoglobin);
      keyResults.push([
        'Hemoglobin',
        `${hb.toFixed(1)} g/dL`,
        hb < 10 ? 'Low' : ''
      ]);
    }

    autoTable(doc, {
      startY: yPos,
      head: [['Test', 'Value', 'Status']],
      body: keyResults,
      theme: 'grid',
      headStyles: { fillColor: [102, 126, 234] },
      margin: { left: 20, right: 20 }
    });

    yPos = (doc as any).lastAutoTable.finalY + 10;

    // CKD Stage
    if (result.ckdStage) {
      doc.setFontSize(13);
      doc.setTextColor(0, 0, 0);
      doc.text('Kidney Function Stage:', 20, yPos);
      yPos += 6;

      doc.setFillColor(219, 234, 254);
      doc.roundedRect(20, yPos - 4, 170, 22, 3, 3, 'F');

      doc.setFontSize(11);
      doc.setTextColor(30, 64, 175);
      doc.text(this.getCKDStageLabel(result.ckdStage), 25, yPos + 3);

      doc.setFontSize(9);
      doc.setTextColor(60, 60, 60);
      const explanation = this.getCKDStageExplanation(result.ckdStage);
      const lines = doc.splitTextToSize(explanation, 160);
      doc.text(lines, 25, yPos + 9);

      yPos += 28;
    }

    // Alerts
    if (result.alerts && result.alerts.length > 0) {
      doc.setFontSize(13);
      doc.setTextColor(0, 0, 0);
      doc.text('Important Alerts:', 20, yPos);
      yPos += 6;

      for (const alert of result.alerts) {
        doc.setFillColor(254, 242, 242);
        doc.roundedRect(20, yPos - 4, 170, 9, 3, 3, 'F');
        doc.setFontSize(9);
        doc.setTextColor(153, 27, 27);
        doc.text(String(alert), 25, yPos + 2);
        yPos += 11;

        // simple page break
        if (yPos > 265) {
          doc.addPage();
          yPos = 20;
        }
      }

      yPos += 3;
    }

    // Advice
    doc.setFontSize(13);
    doc.setTextColor(0, 0, 0);
    doc.text('What should you do?', 20, yPos);
    yPos += 6;

    const advice = this.getActionAdvice(result);
    for (const item of advice) {
      doc.setFillColor(240, 253, 244);
      doc.roundedRect(20, yPos - 4, 170, 9, 3, 3, 'F');
      doc.setFontSize(9);
      doc.setTextColor(6, 95, 70);
      doc.text(item, 25, yPos + 2);
      yPos += 11;

      if (yPos > 265) {
        doc.addPage();
        yPos = 20;
      }
    }

    // Footer
    doc.setFontSize(8);
    doc.setTextColor(150, 150, 150);
    doc.text('Generated by PediaLink', 20, 285);
  }

  // =========================
  // DOCTOR PDF (DETAILED)
  // =========================
  private generateDoctorPDF(doc: jsPDF, result: LabResult): void {
    let yPos = 20;

    // Logo
    if (this.logoDataUrl) {
      try {
        doc.addImage(this.logoDataUrl, 'PNG', 20, yPos - 5, 30, 15);
        yPos += 12;
      } catch (e) {
        console.warn('Could not add logo to PDF', e);
      }
    }

    // Header
    doc.setFontSize(20);
    doc.setTextColor(102, 126, 234);
    doc.text('PediaLink', 55, yPos);

    yPos += 10;
    doc.setFontSize(14);
    doc.setTextColor(0, 0, 0);
    doc.text('Laboratory Test Results - Medical Report', 20, yPos);

    // Info row
    yPos += 8;
    doc.setFontSize(10);
    doc.setTextColor(90, 90, 90);
    const dateStr = result.testDate ? new Date(result.testDate as any).toLocaleString() : 'N/A';
    doc.text(`Test Date: ${dateStr}`, 20, yPos);
    doc.text(`Test Type: ${String(result.testType ?? 'N/A')}`, 120, yPos);

    yPos += 5;
    doc.text(`Laboratory: ${String((result as any).laboratoryName ?? 'N/A')}`, 20, yPos);
    doc.text(`Status: ${result.isAbnormal ? 'Needs Attention' : 'Normal'}`, 120, yPos);

    yPos += 12;

    // Blood Results
    if (result.testType === TestType.BLOOD) {
      doc.setFontSize(13);
      doc.setTextColor(0, 0, 0);
      doc.text('Blood Test Results:', 20, yPos);
      yPos += 6;

      const rows: Array<[string, string]> = [];
      if (result.creatinine !== null && result.creatinine !== undefined) rows.push(['Creatinine', `${Number(result.creatinine).toFixed(2)} mg/dL`]);
      if (result.eGFR !== null && result.eGFR !== undefined) rows.push(['eGFR (Calculated)', `${Number(result.eGFR).toFixed(1)} mL/min/1.73m²`]);
      if ((result as any).urea !== null && (result as any).urea !== undefined) rows.push(['Urea', `${Number((result as any).urea).toFixed(1)} mg/dL`]);
      if ((result as any).sodium !== null && (result as any).sodium !== undefined) rows.push(['Sodium', `${Number((result as any).sodium).toFixed(1)} mmol/L`]);
      if (result.potassium !== null && result.potassium !== undefined) rows.push(['Potassium', `${Number(result.potassium).toFixed(1)} mmol/L`]);
      if ((result as any).calcium !== null && (result as any).calcium !== undefined) rows.push(['Calcium', `${Number((result as any).calcium).toFixed(1)} mg/dL`]);
      if ((result as any).phosphorus !== null && (result as any).phosphorus !== undefined) rows.push(['Phosphorus', `${Number((result as any).phosphorus).toFixed(1)} mg/dL`]);
      if (result.hemoglobin !== null && result.hemoglobin !== undefined) rows.push(['Hemoglobin', `${Number(result.hemoglobin).toFixed(1)} g/dL`]);
      if ((result as any).albumin !== null && (result as any).albumin !== undefined) rows.push(['Albumin', `${Number((result as any).albumin).toFixed(1)} g/dL`]);
      if ((result as any).bicarbonate !== null && (result as any).bicarbonate !== undefined) rows.push(['Bicarbonate', `${Number((result as any).bicarbonate).toFixed(1)} mmol/L`]);

      autoTable(doc, {
        startY: yPos,
        head: [['Parameter', 'Value']],
        body: rows,
        theme: 'striped',
        headStyles: { fillColor: [102, 126, 234] },
        margin: { left: 20, right: 20 }
      });

      yPos = (doc as any).lastAutoTable.finalY + 10;
    }

    // Urine Results
    if (result.testType === TestType.URINE) {
      doc.setFontSize(13);
      doc.setTextColor(0, 0, 0);
      doc.text('Urine Test Results:', 20, yPos);
      yPos += 6;

      const rows: Array<[string, string]> = [];
      if ((result as any).urineProtein !== null && (result as any).urineProtein !== undefined) rows.push(['Protein', `${Number((result as any).urineProtein).toFixed(2)} g/24h`]);
      if ((result as any).urineCreatinine !== null && (result as any).urineCreatinine !== undefined) rows.push(['Creatinine', `${Number((result as any).urineCreatinine).toFixed(1)} mg/dL`]);
      if ((result as any).urineAlbumin !== null && (result as any).urineAlbumin !== undefined) rows.push(['Albumin', `${Number((result as any).urineAlbumin).toFixed(1)} mg/L`]);
      if ((result as any).urineAspect) rows.push(['Aspect', String((result as any).urineAspect)]);
      if ((result as any).proteinCreatinineRatio !== null && (result as any).proteinCreatinineRatio !== undefined) rows.push(['Protein/Creatinine Ratio', `${Number((result as any).proteinCreatinineRatio).toFixed(2)}`]);

      autoTable(doc, {
        startY: yPos,
        head: [['Parameter', 'Value']],
        body: rows,
        theme: 'striped',
        headStyles: { fillColor: [102, 126, 234] },
        margin: { left: 20, right: 20 }
      });

      yPos = (doc as any).lastAutoTable.finalY + 10;
    }

    // CKD
    if (result.ckdStage) {
      doc.setFontSize(13);
      doc.setTextColor(0, 0, 0);
      doc.text('CKD Classification:', 20, yPos);
      yPos += 6;

      doc.setFillColor(219, 234, 254);
      doc.roundedRect(20, yPos - 4, 170, 14, 3, 3, 'F');

      doc.setFontSize(10);
      doc.setTextColor(30, 64, 175);
      doc.text(`Stage: ${this.getCKDStageLabel(result.ckdStage)}`, 25, yPos + 2);

      if (result.eGFR !== null && result.eGFR !== undefined) {
        doc.text(`eGFR: ${Number(result.eGFR).toFixed(1)} mL/min/1.73m²`, 25, yPos + 8);
      }

      yPos += 18;
    }

    // Notes
    if (result.doctorNotes) {
      doc.setFontSize(13);
      doc.setTextColor(0, 0, 0);
      doc.text(`Doctor's Notes:`, 20, yPos);
      yPos += 6;

      doc.setFillColor(254, 243, 199);
      const notesLines = doc.splitTextToSize(String(result.doctorNotes), 160);
      const notesHeight = notesLines.length * 5 + 8;
      doc.roundedRect(20, yPos - 4, 170, notesHeight, 3, 3, 'F');

      doc.setFontSize(9);
      doc.setTextColor(120, 53, 15);
      doc.text(notesLines, 25, yPos + 2);

      yPos += notesHeight + 4;
    }

    // Footer
    doc.setFontSize(8);
    doc.setTextColor(150, 150, 150);
    doc.text('Generated by PediaLink - For professional use only', 20, 285);
  }

  // =========================
  // HELPERS
  // =========================
  private getEGFRStatus(eGFR: number): string {
    if (eGFR >= 90) return 'Normal';
    if (eGFR >= 60) return 'Mild';
    if (eGFR >= 30) return 'Moderate';
    if (eGFR >= 15) return 'Severe';
    return 'Critical';
  }

  private getCKDStageLabel(stage: CKDStage): string {
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

  private getCKDStageExplanation(stage: CKDStage): string {
    const explanations: Record<CKDStage, string> = {
      [CKDStage.STAGE_1]: 'Kidney function is normal or near normal.',
      [CKDStage.STAGE_2]: 'Kidney function is slightly reduced, but still working well.',
      [CKDStage.STAGE_3A]: 'Kidney function is moderately reduced. Regular monitoring is important.',
      [CKDStage.STAGE_3B]: 'Kidney function is moderately to severely reduced. Close medical follow-up is needed.',
      [CKDStage.STAGE_4]: 'Kidney function is severely reduced. Preparation for possible dialysis may be discussed.',
      [CKDStage.STAGE_5]: 'Kidney failure. Dialysis or transplant may be needed.',
      [CKDStage.UNKNOWN]: 'Unable to determine kidney function stage.'
    };
    return explanations[stage] || '';
  }

  private getActionAdvice(result: LabResult): string[] {
    const advice: string[] = [];

    if (result.isAbnormal) {
      advice.push('Contact your doctor to discuss these results');
      advice.push('Keep track of symptoms and report any changes');
      advice.push('Continue prescribed medications as directed');

      if (result.potassium !== null && result.potassium !== undefined && Number(result.potassium) > 5.5) {
        advice.push('Limit high-potassium foods');
      }
      if (result.hemoglobin !== null && result.hemoglobin !== undefined && Number(result.hemoglobin) < 10) {
        advice.push('Include iron-rich foods in diet');
      }
    } else {
      advice.push('Continue current treatment plan');
      advice.push('Schedule next check-up as recommended');
      advice.push('Maintain good hydration');
    }

    return advice;
  }
}