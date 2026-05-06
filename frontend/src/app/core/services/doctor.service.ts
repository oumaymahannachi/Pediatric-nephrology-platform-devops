import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Appointment } from '../models/appointment.model';
import { Child } from '../models/child.model';
import { GrowthMeasurement, DietaryRestriction, NutritionalPlan } from '../models/treatment.model';

@Injectable({ providedIn: 'root' })
export class DoctorService {
  private readonly API = `${environment.apiUrl}/doctor`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<any> {
    return this.http.get<any>(`${this.API}/dashboard`);
  }

  getPatients(): Observable<Child[]> {
    return this.http.get<Child[]>(`${this.API}/patients`);
  }

  getPatientMeasurements(childId: string): Observable<GrowthMeasurement[]> {
    return this.http.get<GrowthMeasurement[]>(`${this.API}/patients/${childId}/measurements`);
  }

  getPatientRestrictions(childId: string): Observable<DietaryRestriction[]> {
    return this.http.get<DietaryRestriction[]>(`${this.API}/patients/${childId}/restrictions`);
  }

  getPatientPlans(childId: string): Observable<NutritionalPlan[]> {
    return this.http.get<NutritionalPlan[]>(`${this.API}/patients/${childId}/plans`);
  }

  getNutritionalPlans(): Observable<NutritionalPlan[]> {
    return this.http.get<NutritionalPlan[]>(`${this.API}/nutritional-plans`);
  }

  createNutritionalPlan(plan: Partial<NutritionalPlan>): Observable<NutritionalPlan> {
    return this.http.post<NutritionalPlan>(`${this.API}/nutritional-plans`, plan);
  }

  updateNutritionalPlan(id: string, plan: Partial<NutritionalPlan>): Observable<NutritionalPlan> {
    return this.http.put<NutritionalPlan>(`${this.API}/nutritional-plans/${id}`, plan);
  }

  deleteNutritionalPlan(id: string): Observable<any> {
    return this.http.delete<any>(`${this.API}/nutritional-plans/${id}`);
  }

  getAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.API}/appointments`);
  }

  getPendingAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.API}/appointments/pending`);
  }

  acceptAppointment(id: string): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.API}/appointments/${id}/accept`, {});
  }

  refuseAppointment(id: string, doctorNotes?: string): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.API}/appointments/${id}/refuse`, { doctorNotes });
  }

  rescheduleAppointment(id: string, proposedDateTime: string, doctorNotes?: string): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.API}/appointments/${id}/reschedule`, { proposedDateTime, doctorNotes });
  }

  completeAppointment(id: string): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.API}/appointments/${id}/complete`, {});
  }

  generateAINutritionalPlan(request: any): Observable<any> {
    return this.http.post<any>(`${this.API}/nutritional-plans/generate-ai`, request);
  }
}
