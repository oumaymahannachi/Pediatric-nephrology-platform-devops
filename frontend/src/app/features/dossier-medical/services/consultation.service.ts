import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Consultation, ConsultationCreateRequest, ConsultationResponseRequest } from '../models/consultation.model';

@Injectable({
  providedIn: 'root'
})
export class ConsultationService {
  private apiUrl = `${environment.apiUrl}/consultations`;

  constructor(private http: HttpClient) {}

  // Parent crée une demande de consultation
  createConsultationRequest(consultation: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/request`, consultation);
  }

  // Médecin accepte la consultation
  acceptConsultation(id: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/accept`, {});
  }

  // Médecin refuse et propose une autre date
  refuseConsultation(id: string, data: ConsultationResponseRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/refuse`, data);
  }

  // Parent accepte la date proposée
  acceptProposedDate(id: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/accept-proposed`, {});
  }

  // Médecin complète la consultation
  completeConsultation(id: string, data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/complete`, data);
  }

  getConsultationsByPatient(patientId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}`);
  }

  getConsultationsByMedecin(medecinId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/medecin/${medecinId}`);
  }

  getPendingConsultations(medecinId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/medecin/${medecinId}/pending`);
  }

  getConsultationById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  deleteConsultation(id: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
