import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DialysisPrescription, DialysisSession } from '../models/dialysis.model';

@Injectable({ providedIn: 'root' })
export class DialysisService {
  private readonly API = `${environment.apiUrl}/dialyses`;

  constructor(private http: HttpClient) {}

  // Prescription endpoints
  createPrescription(prescription: Partial<DialysisPrescription>): Observable<DialysisPrescription> {
    return this.http.post<DialysisPrescription>(`${this.API}/prescriptions`, prescription);
  }

  updatePrescription(id: string, prescription: Partial<DialysisPrescription>): Observable<DialysisPrescription> {
    return this.http.put<DialysisPrescription>(`${this.API}/prescriptions/${id}`, prescription);
  }

  deactivatePrescription(id: string): Observable<any> {
    return this.http.delete(`${this.API}/prescriptions/${id}`);
  }

  getPrescription(id: string): Observable<DialysisPrescription> {
    return this.http.get<DialysisPrescription>(`${this.API}/prescriptions/${id}`);
  }

  getPrescriptionsByPatient(patientId: string): Observable<DialysisPrescription[]> {
    return this.http.get<DialysisPrescription[]>(`${this.API}/prescriptions/patient/${patientId}`);
  }

  getActivePrescriptionsByPatient(patientId: string): Observable<DialysisPrescription[]> {
    return this.http.get<DialysisPrescription[]>(`${this.API}/prescriptions/patient/${patientId}/active`);
  }

  getPrescriptionsByDoctor(medecinId: string): Observable<DialysisPrescription[]> {
    return this.http.get<DialysisPrescription[]>(`${this.API}/prescriptions/doctor/${medecinId}`);
  }

  // Session endpoints
  scheduleSession(session: Partial<DialysisSession>): Observable<DialysisSession> {
    return this.http.post<DialysisSession>(`${this.API}/sessions`, session);
  }

  startSession(id: string): Observable<DialysisSession> {
    return this.http.post<DialysisSession>(`${this.API}/sessions/${id}/start`, {});
  }

  completeSession(id: string, sessionData: Partial<DialysisSession>): Observable<DialysisSession> {
    return this.http.post<DialysisSession>(`${this.API}/sessions/${id}/complete`, sessionData);
  }

  cancelSession(id: string, reason: string): Observable<DialysisSession> {
    return this.http.post<DialysisSession>(`${this.API}/sessions/${id}/cancel`, { reason });
  }

  rescheduleSession(id: string, scheduledDate: string): Observable<DialysisSession> {
    return this.http.post<DialysisSession>(`${this.API}/sessions/${id}/reschedule`, { scheduledDate });
  }

  getSession(id: string): Observable<DialysisSession> {
    return this.http.get<DialysisSession>(`${this.API}/sessions/${id}`);
  }

  getSessionsByPatient(patientId: string): Observable<DialysisSession[]> {
    return this.http.get<DialysisSession[]>(`${this.API}/sessions/patient/${patientId}`);
  }

  getSessionsByPrescription(prescriptionId: string): Observable<DialysisSession[]> {
    return this.http.get<DialysisSession[]>(`${this.API}/sessions/prescription/${prescriptionId}`);
  }

  getSessionsByDoctor(medecinId: string): Observable<DialysisSession[]> {
    return this.http.get<DialysisSession[]>(`${this.API}/sessions/doctor/${medecinId}`);
  }

  getSessionsByDateRange(start: string, end: string): Observable<DialysisSession[]> {
    return this.http.get<DialysisSession[]>(`${this.API}/sessions/range?start=${start}&end=${end}`);
  }
}
