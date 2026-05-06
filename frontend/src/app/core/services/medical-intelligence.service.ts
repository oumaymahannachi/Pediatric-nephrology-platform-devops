import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ParameterAnalysis {
  parameterName: string;
  value: number;
  unit: string;
  normalMin: number;
  normalMax: number;
  status: 'NORMAL' | 'LOW' | 'HIGH' | 'CRITICAL_LOW' | 'CRITICAL_HIGH' | 'UNKNOWN';
  interpretation: string;
  ageGroup: string;
}

export interface BiologicalInterpretation {
  patientId: string;
  patientAgeMonths: number;
  patientWeightKg: number;
  analyses: ParameterAnalysis[];
  criticalAlerts: string[];
  recommendations: string[];
  creatinineUrineRatio: number;
  acuteKidneyInjuryDetected: boolean;
  akiSeverity: string;
  analysisDate: string;
}

export interface ConsultationSnapshot {
  date: string;
  diagnostic: string;
  gfrValue: number;
  creatinineValue: number;
  ureaValue: number;
  ckdStage: string;
  symptoms: string[];
}

export interface PathologyEvolution {
  patientId: string;
  gfrTrend: string;
  creatinineTrend: string;
  ureaTrend: string;
  overallProgression: string;
  clinicalSummary: string;
  recommendations: string[];
  consultationHistory: ConsultationSnapshot[];
  analysisDate: string;
}

export interface MedicalReminder {
  id: string;
  patientId: string;
  patientName: string;
  medecinId: string;
  type: string;
  status: string;
  title: string;
  message: string;
  dueDate: string;
  createdAt: string;
  daysSinceLastConsultation: number;
  chronic: boolean;
}

@Injectable({ providedIn: 'root' })
export class MedicalIntelligenceService {
  private readonly API = `${environment.apiUrl}/medical-intelligence`;

  constructor(private http: HttpClient) {}

  getBiologicalInterpretation(patientId: string, ageMonths: number, weightKg: number): Observable<BiologicalInterpretation> {
    return this.http.get<BiologicalInterpretation>(
      `${this.API}/biological-interpretation/${patientId}?ageMonths=${ageMonths}&weightKg=${weightKg}`
    );
  }

  getPathologyEvolution(patientId: string): Observable<PathologyEvolution> {
    return this.http.get<PathologyEvolution>(`${this.API}/pathology-evolution/${patientId}`);
  }

  generateReminders(patientId: string, medecinId: string): Observable<MedicalReminder[]> {
    return this.http.post<MedicalReminder[]>(`${this.API}/reminders/generate/${patientId}`, { medecinId });
  }

  getDoctorReminders(medecinId: string): Observable<MedicalReminder[]> {
    return this.http.get<MedicalReminder[]>(`${this.API}/reminders/doctor/${medecinId}`);
  }

  getPatientReminders(patientId: string): Observable<MedicalReminder[]> {
    return this.http.get<MedicalReminder[]>(`${this.API}/reminders/patient/${patientId}`);
  }

  acknowledgeReminder(reminderId: string): Observable<MedicalReminder> {
    return this.http.put<MedicalReminder>(`${this.API}/reminders/${reminderId}/acknowledge`, {});
  }

  countPendingReminders(medecinId: string): Observable<{ pending: number }> {
    return this.http.get<{ pending: number }>(`${this.API}/reminders/doctor/${medecinId}/count`);
  }
}
