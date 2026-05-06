import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProgressService {
  private apiUrl = `${environment.apiUrl}/traitements/api/v1/progress`;

  constructor(private http: HttpClient) {}

  logProgress(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/log`, data);
  }

  getProgressByTreatment(treatmentId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/treatment/${treatmentId}`);
  }

  getProgressByPatient(patientId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/patient/${patientId}`);
  }

  analyzeProgress(treatmentId: string, jours: number = 30): Observable<any> {
    return this.http.get(`${this.apiUrl}/analyze/${treatmentId}?jours=${jours}`);
  }
}
