import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TreatmentAiService {
  private apiUrl = `${environment.apiUrl}/v1/treatments/ai`;

  constructor(private http: HttpClient) {}

  /**
   * Translate entire treatment
   */
  translateTreatment(treatment: any, targetLanguage: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/translate`, {
      treatment,
      targetLanguage
    });
  }

  /**
   * Get supported languages
   */
  getSupportedLanguages(): Observable<any> {
    return this.http.get(`${this.apiUrl}/translate/languages`);
  }
}
