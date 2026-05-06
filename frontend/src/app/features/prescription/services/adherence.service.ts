import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface AdherenceLog {
  id: string;
  prescriptionId: string;
  patientId: string;
  medicamentNom: string;
  datePrise: string;
  prise: boolean;
  raison?: string;
  notes?: string;
  effetsSecondaires?: string;
  createdAt: string;
}

export interface AdherenceStats {
  periode: string;
  totalPrevu: number;
  totalPris: number;
  totalOublie: number;
  tauxAdherence: number;
  niveau: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdherenceService {
  private apiUrl = `${environment.apiUrl}/v1/adherence`;

  constructor(private http: HttpClient) {}

  logPriseMedicament(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/log`, data);
  }

  getLogsPatient(patientId: string): Observable<AdherenceLog[]> {
    return this.http.get<AdherenceLog[]>(`${this.apiUrl}/patient/${patientId}`);
  }

  getLogsPrescription(prescriptionId: string): Observable<AdherenceLog[]> {
    return this.http.get<AdherenceLog[]>(`${this.apiUrl}/prescription/${prescriptionId}`);
  }

  getStatistiques(patientId: string, jours: number = 30): Observable<AdherenceStats> {
    return this.http.get<AdherenceStats>(`${this.apiUrl}/stats/${patientId}?jours=${jours}`);
  }
}
