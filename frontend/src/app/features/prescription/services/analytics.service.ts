import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private apiUrl = `${environment.apiUrl}/prescriptions/api/v1/analytics`;

  constructor(private http: HttpClient) {}

  getStatistiquesMedecin(medecinId: string, jours: number = 30): Observable<any> {
    return this.http.get(`${this.apiUrl}/medecin/${medecinId}?jours=${jours}`);
  }

  getTopMedicaments(jours: number = 30, limit: number = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/medicaments/top?jours=${jours}&limit=${limit}`);
  }

  getInteractionsFrequentes(): Observable<any> {
    return this.http.get(`${this.apiUrl}/interactions/frequentes`);
  }

  getHistoriquePatient(patientId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/patient/${patientId}/historique`);
  }
}
