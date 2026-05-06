import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AiService } from '../services/ai.service';

@Component({
  selector: 'app-ocr-scanner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ocr-scanner.component.html',
  styleUrls: ['./ocr-scanner.component.css']
})
export class OcrScannerComponent {
  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isProcessing = false;
  extractedData: any = null;
  error: string | null = null;

  constructor(
    private aiService: AiService,
    private router: Router
  ) {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.error = null;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewUrl = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  scanPrescription(): void {
    if (!this.selectedFile) {
      this.error = 'Please select an image first';
      return;
    }

    this.isProcessing = true;
    this.error = null;
    this.extractedData = null;

    console.log('Scanning prescription...', this.selectedFile.name);

    this.aiService.extractPrescriptionFromImage(this.selectedFile).subscribe({
      next: (response) => {
        console.log('OCR Response:', response);
        this.isProcessing = false;
        
        if (response && response.success) {
          this.extractedData = response;
          console.log('Extracted data:', this.extractedData);
        } else {
          this.error = response?.message || 'Failed to extract prescription data';
          console.error('Extraction failed:', response);
        }
      },
      error: (err) => {
        this.isProcessing = false;
        console.error('OCR error:', err);
        
        if (err.error && err.error.message) {
          this.error = err.error.message;
        } else if (err.status === 0) {
          this.error = 'Cannot connect to server. Please check if the backend is running.';
        } else {
          this.error = 'Error processing image. Please try again.';
        }
      }
    });
  }

  usePrescription(): void {
    if (this.extractedData) {
      // Navigate to prescription form with extracted data
      this.router.navigate(['/doctor/prescriptions/new'], {
        state: { extractedData: this.extractedData }
      });
    }
  }

  reset(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.extractedData = null;
    this.error = null;
  }

  triggerFileInput(): void {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    fileInput?.click();
  }
}
