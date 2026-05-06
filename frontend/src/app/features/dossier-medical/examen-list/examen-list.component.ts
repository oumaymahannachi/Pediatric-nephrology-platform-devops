import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ExaminationService } from '../../../core/services/examination.service';
import { AuthService } from '../../../core/services/auth.service';
import { OcrService, LabResultOCR, ImagingResultOCR } from '../../../core/services/ocr.service';
import { BloodTest, LabResult, MedicalImaging } from '../../../core/models/examination.model';

@Component({
  selector: 'app-examen-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './examen-list.component.html',
  styleUrls: ['./examen-list.component.css']
})
export class ExamenListComponent implements OnInit, OnChanges {
  @Input() patientId!: string;
  
  activeSubTab: 'blood' | 'lab' | 'imaging' | 'history' = 'blood';
  loading = false;
  currentUserId = '';

  bloodTests: BloodTest[] = [];
  labResults: LabResult[] = [];
  medicalImagings: MedicalImaging[] = [];

  showBloodTestModal = false;
  showLabResultModal = false;
  showImagingModal = false;

  bloodTestForm!: FormGroup;
  labResultForm!: FormGroup;
  imagingForm!: FormGroup;

  editingBloodTest: BloodTest | null = null;
  editingLabResult: LabResult | null = null;
  editingImaging: MedicalImaging | null = null;

  selectedFiles: File[] = [];
  uploadedFileUrls: string[] = [];

  historyFilter: string = 'all';
  dateRange: string = 'all';
  allHistory: any[] = [];
  filteredHistory: any[] = [];

  showOcrModal = false;
  ocrProcessing = false;
  ocrResult: LabResultOCR | ImagingResultOCR | null = null;
  ocrType: 'lab' | 'imaging' = 'lab';
  ocrFile: File | null = null;
  
  Object = Object;

  constructor(
    private examinationService: ExaminationService,
    private authService: AuthService,
    private ocrService: OcrService,
    private fb: FormBuilder
  ) {
    this.initForms();
    this.authService.currentUser$.subscribe(user => {
      this.currentUserId = user?.id || '';
    });
  }

  ngOnInit(): void {
    if (this.patientId) {
      this.loadData();
    }
  }

  ngOnChanges(): void {
    if (this.patientId) {
      this.loadData();
    }
  }

  initForms(): void {
    this.bloodTestForm = this.fb.group({
      testDate: ['', Validators.required],
      testType: ['CBC', Validators.required],
      laboratoryName: [''],
      notes: [''],
      interpretation: [''],
      abnormal: [false],
      creatinine: [null],
      urea: [null],
      potassium: [null],
      sodium: [null],
      hemoglobin: [null]
    });

    this.labResultForm = this.fb.group({
      testDate: ['', Validators.required],
      testType: ['URINALYSIS', Validators.required],
      testName: ['', Validators.required],
      findings: [''],
      result: ['NORMAL'],
      details: [''],
      laboratoryName: [''],
      specimenType: [''],
      notes: [''],
      abnormal: [false]
    });

    this.imagingForm = this.fb.group({
      imagingDate: ['', Validators.required],
      imagingType: ['XRAY', Validators.required],
      bodyPart: ['', Validators.required],
      indication: [''],
      findings: [''],
      impression: [''],
      recommendation: [''],
      radiologistName: [''],
      performedBy: [''],
      facilityName: [''],
      urgencyLevel: ['NORMAL'],
      followUpRequired: [false],
      followUpDate: [''],
      status: ['PENDING'],
      notes: [''],
      abnormal: [false]
    });
  }

  loadData(): void {
    this.loading = true;
    if (this.activeSubTab === 'blood') {
      this.loadBloodTests();
    } else if (this.activeSubTab === 'lab') {
      this.loadLabResults();
    } else if (this.activeSubTab === 'imaging') {
      this.loadMedicalImaging();
    } else if (this.activeSubTab === 'history') {
      this.loadHistory();
    }
  }

  loadBloodTests(): void {
    this.examinationService.getBloodTestsByPatient(this.patientId).subscribe({
      next: (tests) => {
        this.bloodTests = tests;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  loadLabResults(): void {
    this.examinationService.getLabResultsByPatient(this.patientId).subscribe({
      next: (results) => {
        this.labResults = results;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  loadMedicalImaging(): void {
    this.examinationService.getMedicalImagingByPatient(this.patientId).subscribe({
      next: (imagings) => {
        console.log('Loaded medical imagings:', imagings);
        imagings.forEach((img, index) => {
          console.log(`Imaging ${index}:`, {
            id: img.id,
            imageUrls: img.imageUrls,
            imageUrlsLength: img.imageUrls?.length || 0,
            documentUrls: img.documentUrls,
            documentUrlsLength: img.documentUrls?.length || 0,
            hasImages: !!(img.imageUrls && img.imageUrls.length > 0),
            hasDocs: !!(img.documentUrls && img.documentUrls.length > 0)
          });
        });
        this.medicalImagings = imagings;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  setSubTab(tab: 'blood' | 'lab' | 'imaging' | 'history'): void {
    this.activeSubTab = tab;
    this.showBloodTestModal = false;
    this.showLabResultModal = false;
    this.showImagingModal = false;
    this.loadData();
  }

  openBloodTestModal(test?: BloodTest): void {
    this.editingBloodTest = test || null;
    if (test) {
      this.bloodTestForm.patchValue(test);
    } else {
      this.bloodTestForm.reset({ testType: 'CBC', abnormal: false });
    }
    this.showBloodTestModal = true;
  }

  saveBloodTest(): void {
    if (this.bloodTestForm.invalid) return;

    const formVal = this.bloodTestForm.value;

    const results: any = {};
    const bioFields = ['creatinine', 'urea', 'potassium', 'sodium', 'hemoglobin'];
    const units: any = {
      creatinine: 'mg/dL', urea: 'mg/dL',
      potassium: 'mEq/L', sodium: 'mEq/L', hemoglobin: 'g/dL'
    };
    bioFields.forEach(field => {
      if (formVal[field] !== null && formVal[field] !== '' && formVal[field] !== undefined) {
        results[field] = { value: parseFloat(formVal[field]), unit: units[field] };
      }
    });

    const data: any = {
      testDate: formVal.testDate,
      testType: formVal.testType,
      laboratoryName: formVal.laboratoryName,
      notes: formVal.notes,
      interpretation: formVal.interpretation,
      abnormal: formVal.abnormal,
      patientId: this.patientId,
      medecinId: this.currentUserId,
      results: Object.keys(results).length > 0 ? results : null
    };

    const request = this.editingBloodTest
      ? this.examinationService.updateBloodTest(this.editingBloodTest.id!, data)
      : this.examinationService.createBloodTest(data);

    request.subscribe({
      next: () => {
        this.showBloodTestModal = false;
        this.loadBloodTests();
      },
      error: (err) => console.error('Error saving blood test:', err)
    });
  }

  deleteBloodTest(id: string): void {
    if (!confirm('Delete this blood test?')) return;
    this.examinationService.deleteBloodTest(id).subscribe({
      next: () => this.loadBloodTests(),
      error: (err) => console.error('Error deleting blood test:', err)
    });
  }

  openLabResultModal(result?: LabResult): void {
    this.editingLabResult = result || null;
    if (result) {
      this.labResultForm.patchValue(result);
    } else {
      this.labResultForm.reset({ testType: 'URINALYSIS', result: 'NORMAL', abnormal: false });
    }
    this.showLabResultModal = true;
  }

  saveLabResult(): void {
    if (this.labResultForm.invalid) return;

    const data = {
      ...this.labResultForm.value,
      patientId: this.patientId,
      medecinId: this.currentUserId
    };

    const request = this.editingLabResult
      ? this.examinationService.updateLabResult(this.editingLabResult.id!, data)
      : this.examinationService.createLabResult(data);

    request.subscribe({
      next: () => {
        this.showLabResultModal = false;
        this.loadLabResults();
      },
      error: (err) => console.error('Error saving lab result:', err)
    });
  }

  deleteLabResult(id: string): void {
    if (!confirm('Delete this lab result?')) return;
    this.examinationService.deleteLabResult(id).subscribe({
      next: () => this.loadLabResults(),
      error: (err) => console.error('Error deleting lab result:', err)
    });
  }

  openImagingModal(imaging?: MedicalImaging): void {
    this.editingImaging = imaging || null;
    this.selectedFiles = [];
    this.uploadedFileUrls = [];
    
    if (imaging) {
      this.imagingForm.patchValue(imaging);
      if (imaging.imageUrls) {
        this.uploadedFileUrls = [...imaging.imageUrls];
      }
      if (imaging.documentUrls) {
        this.uploadedFileUrls = [...this.uploadedFileUrls, ...imaging.documentUrls];
      }
    } else {
      this.imagingForm.reset({ 
        imagingType: 'XRAY', 
        urgencyLevel: 'NORMAL',
        followUpRequired: false,
        status: 'PENDING',
        abnormal: false 
      });
    }
    this.showImagingModal = true;
  }

  saveImaging(): void {
    if (this.imagingForm.invalid) return;

    const imageUrls = this.uploadedFileUrls.filter(url => this.isImageFile(url));
    const documentUrls = this.uploadedFileUrls.filter(url => !this.isImageFile(url));

    console.log('Saving imaging with files:', {
      totalFiles: this.uploadedFileUrls.length,
      imageUrls: imageUrls.length,
      documentUrls: documentUrls.length
    });

    const data = {
      ...this.imagingForm.value,
      patientId: this.patientId,
      medecinId: this.currentUserId,
      imageUrls: imageUrls,
      documentUrls: documentUrls
    };

    const request = this.editingImaging
      ? this.examinationService.updateMedicalImaging(this.editingImaging.id!, data)
      : this.examinationService.createMedicalImaging(data);

    request.subscribe({
      next: (result) => {
        console.log('Imaging saved successfully:', result);
        this.showImagingModal = false;
        this.selectedFiles = [];
        this.uploadedFileUrls = [];
        this.loadMedicalImaging();
      },
      error: (err) => console.error('Error saving imaging:', err)
    });
  }

  deleteImaging(id: string): void {
    if (!confirm('Delete this imaging record?')) return;
    this.examinationService.deleteMedicalImaging(id).subscribe({
      next: () => this.loadMedicalImaging(),
      error: (err) => console.error('Error deleting imaging:', err)
    });
  }

  onFileSelect(event: any): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      this.selectedFiles = Array.from(files);
      
      const filePromises: Promise<string>[] = [];
      
      for (let file of this.selectedFiles) {
        const promise = new Promise<string>((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = (e: any) => {
            resolve(e.target.result);
          };
          reader.onerror = reject;
          reader.readAsDataURL(file);
        });
        filePromises.push(promise);
      }
      
      Promise.all(filePromises).then(dataUrls => {
        this.uploadedFileUrls = [...this.uploadedFileUrls, ...dataUrls];
      }).catch(err => {
        console.error('Error reading files:', err);
      });
    }
  }

  removeFile(index: number): void {
    this.uploadedFileUrls.splice(index, 1);
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

  hasAttachments(imaging: MedicalImaging): boolean {
    const hasImages = imaging.imageUrls && Array.isArray(imaging.imageUrls) && imaging.imageUrls.length > 0;
    const hasDocs = imaging.documentUrls && Array.isArray(imaging.documentUrls) && imaging.documentUrls.length > 0;
    return !!(hasImages || hasDocs);
  }

  loadHistory(): void {
    this.loading = true;
    Promise.all([
      this.examinationService.getBloodTestsByPatient(this.patientId).toPromise(),
      this.examinationService.getLabResultsByPatient(this.patientId).toPromise(),
      this.examinationService.getMedicalImagingByPatient(this.patientId).toPromise()
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

      const beforeCount = filtered.length;
      filtered = filtered.filter(item => {
        const itemDate = new Date(item.date);
        const isIncluded = itemDate >= cutoffDate && itemDate <= now;
        return isIncluded;
      });

      console.log('Date filter applied:', {
        dateRange: this.dateRange,
        cutoffDate: cutoffDate.toLocaleDateString(),
        now: now.toLocaleDateString(),
        beforeFilter: beforeCount,
        afterFilter: filtered.length,
        itemDates: this.allHistory.map(item => ({
          title: item.title,
          date: new Date(item.date).toLocaleDateString(),
          isFuture: new Date(item.date) > now,
          isPast: new Date(item.date) < cutoffDate,
          included: new Date(item.date) >= cutoffDate && new Date(item.date) <= now
        }))
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

  openOcrModal(type: 'lab' | 'imaging'): void {
    this.ocrType = type;
    this.ocrResult = null;
    this.ocrFile = null;
    this.showOcrModal = true;
  }

  onOcrFileSelect(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.ocrFile = file;
      this.processOcr();
    }
  }

  processOcr(): void {
    if (!this.ocrFile) return;

    this.ocrProcessing = true;
    this.ocrResult = null;

    if (this.ocrType === 'lab') {
      this.ocrService.parseLabResults(this.ocrFile).subscribe({
        next: (result: any) => {
          this.ocrResult = result;
          this.ocrProcessing = false;
          
          if (result.confidenceScore > 0.6) {
            this.autoFillFromOcr(result);
          }
        },
        error: (err: any) => {
          console.error('OCR error:', err);
          this.ocrProcessing = false;
          alert('Error processing document. Please try again or enter manually.');
        }
      });
    } else {
      this.ocrService.parseImagingResults(this.ocrFile).subscribe({
        next: (result: any) => {
          this.ocrResult = result;
          this.ocrProcessing = false;
          
          if (result.confidenceScore > 0.6) {
            this.autoFillFromOcr(result);
          }
        },
        error: (err: any) => {
          console.error('OCR error:', err);
          this.ocrProcessing = false;
          alert('Error processing document. Please try again or enter manually.');
        }
      });
    }
  }

  autoFillFromOcr(result: LabResultOCR | ImagingResultOCR): void {
    if (this.ocrType === 'lab' && this.isLabResultOCR(result)) {
      const labResult = result as LabResultOCR;
      
      this.labResultForm.patchValue({
        testDate: labResult.testDate || '',
        laboratoryName: labResult.laboratoryName || '',
        notes: `OCR Extracted (Confidence: ${(labResult.confidenceScore * 100).toFixed(0)}%)\n\nRaw Text:\n${labResult.rawText}`
      });

      if (Object.keys(labResult.extractedValues).length > 0) {
        this.bloodTestForm.patchValue({
          testDate: labResult.testDate || '',
          laboratoryName: labResult.laboratoryName || '',
          notes: this.formatLabValues(labResult.extractedValues) + 
                 `\n\nOCR Confidence: ${(labResult.confidenceScore * 100).toFixed(0)}%`
        });
      }
    } else if (this.ocrType === 'imaging' && this.isImagingResultOCR(result)) {
      const imagingResult = result as ImagingResultOCR;
      
      this.imagingForm.patchValue({
        imagingDate: imagingResult.imagingDate || '',
        imagingType: imagingResult.imagingType || 'XRAY',
        findings: imagingResult.findings || '',
        impression: imagingResult.impression || '',
        recommendation: imagingResult.recommendation || '',
        radiologistName: imagingResult.radiologistName || '',
        notes: `OCR Extracted (Confidence: ${(imagingResult.confidenceScore * 100).toFixed(0)}%)`
      });
    }
  }

  formatLabValues(values: { [key: string]: string }): string {
    let formatted = 'Extracted Values:\n';
    for (const [key, value] of Object.entries(values)) {
      formatted += `${key}: ${value}\n`;
    }
    return formatted;
  }

  isLabResultOCR(result: any): result is LabResultOCR {
    return 'extractedValues' in result;
  }

  isImagingResultOCR(result: any): result is ImagingResultOCR {
    return 'imagingType' in result;
  }

  useOcrResult(): void {
    if (!this.ocrResult) return;

    if (this.ocrType === 'lab') {
      this.showOcrModal = false;
      this.openLabResultModal();
    } else {
      this.showOcrModal = false;
      this.openImagingModal();
    }
  }

  closeOcrModal(): void {
    this.showOcrModal = false;
    this.ocrResult = null;
    this.ocrFile = null;
  }
}
