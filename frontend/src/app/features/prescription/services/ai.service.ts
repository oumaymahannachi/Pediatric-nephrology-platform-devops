import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private apiUrl = `${environment.apiUrl}/prescriptions/ai`;

  constructor(private http: HttpClient) {}

  /**
   * Extract prescription from image using OCR
   */
  extractPrescriptionFromImage(imageFile: File): Observable<any> {
    const formData = new FormData();
    formData.append('image', imageFile);
    
    return this.http.post(`${this.apiUrl}/ocr/extract`, formData);
  }

  /**
   * Translate text to target language
   */
  translateText(text: string, targetLanguage: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/translate`, {
      text,
      targetLanguage
    });
  }

  /**
   * Translate text to multiple languages
   */
  translateToMultiple(text: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/translate/multiple`, { text });
  }

  /**
   * Get supported languages
   */
  getSupportedLanguages(): Observable<any> {
    return this.http.get(`${this.apiUrl}/translate/languages`);
  }

  /**
   * Translate entire prescription
   */
  translatePrescription(prescription: any, targetLanguage: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/translate/prescription`, {
      prescription,
      targetLanguage
    });
  }

  /**
   * Get AI interpretation of prescription in simple language
   */
  interpretPrescription(prescriptionId: string, language: string = 'en'): Observable<any> {
    return this.http.post(`${this.apiUrl}/interpret/${prescriptionId}?language=${language}`, {});
  }
}
