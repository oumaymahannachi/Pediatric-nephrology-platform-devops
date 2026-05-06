import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LabResultOCR {
  rawText: string;
  extractedValues: { [key: string]: string };
  testDate: string;
  laboratoryName: string;
  confidenceScore: number;
}

export interface ImagingResultOCR {
  rawText: string;
  imagingType: string;
  findings: string;
  impression: string;
  recommendation: string;
  imagingDate: string;
  radiologistName: string;
  confidenceScore: number;
}

@Injectable({
  providedIn: 'root'
})
export class OcrService {
  private apiUrl = `${environment.apiUrl}/ocr`;

  constructor(private http: HttpClient) {}

  extractTextFromImage(file: File): Observable<{ extractedText: string; success: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ extractedText: string; success: string }>(
      `${this.apiUrl}/extract-text/image`,
      formData
    );
  }

  extractTextFromPDF(file: File): Observable<{ extractedText: string; success: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ extractedText: string; success: string }>(
      `${this.apiUrl}/extract-text/pdf`,
      formData
    );
  }

  extractTextFromBase64(base64Data: string): Observable<{ extractedText: string; success: string }> {
    return this.http.post<{ extractedText: string; success: string }>(
      `${this.apiUrl}/extract-text/base64`,
      { base64Data }
    );
  }

  parseLabResults(file: File): Observable<LabResultOCR> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<LabResultOCR>(
      `${this.apiUrl}/parse/lab-results`,
      formData
    );
  }

  parseImagingResults(file: File): Observable<ImagingResultOCR> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ImagingResultOCR>(
      `${this.apiUrl}/parse/imaging-results`,
      formData
    );
  }

  parseLabResultsFromBase64(base64Data: string): Observable<LabResultOCR> {
    return this.http.post<LabResultOCR>(
      `${this.apiUrl}/parse/lab-results/base64`,
      { base64Data }
    );
  }

  parseImagingResultsFromBase64(base64Data: string): Observable<ImagingResultOCR> {
    return this.http.post<ImagingResultOCR>(
      `${this.apiUrl}/parse/imaging-results/base64`,
      { base64Data }
    );
  }
}
