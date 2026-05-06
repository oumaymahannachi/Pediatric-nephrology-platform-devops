import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Child, DoctorInfo } from '../models/child.model';
import { Appointment } from '../models/appointment.model';
import { ApiMessage } from '../models/auth.models';
import { GrowthMeasurement, DietaryRestriction, NutritionalPlan } from '../models/treatment.model';

@Injectable({ providedIn: 'root' })
export class ParentService {
  private readonly API = `${environment.apiUrl}/parent`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<any> {
    return this.http.get<any>(`${this.API}/dashboard`);
  }

  getChildren(): Observable<Child[]> {
    return this.http.get<Child[]>(`${this.API}/children`);
  }

  addChild(child: Partial<Child>): Observable<Child> {
    return this.http.post<Child>(`${this.API}/children`, child);
  }

  updateChild(id: string, child: Partial<Child>): Observable<Child> {
    return this.http.put<Child>(`${this.API}/children/${id}`, child);
  }

  deleteChild(id: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.API}/children/${id}`);
  }

  assignDoctor(childId: string, doctorId: string): Observable<Child> {
    return this.http.post<Child>(`${this.API}/children/${childId}/doctors/${doctorId}`, {});
  }

  removeDoctor(childId: string, doctorId: string): Observable<Child> {
    return this.http.delete<Child>(`${this.API}/children/${childId}/doctors/${doctorId}`);
  }

  getAvailableDoctors(): Observable<DoctorInfo[]> {
    return this.http.get<DoctorInfo[]>(`${environment.apiUrl}/users/doctors`);
  }

  getAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.API}/appointments`);
  }

  createAppointment(apt: Partial<Appointment>): Observable<Appointment> {
    return this.http.post<Appointment>(`${this.API}/appointments`, apt);
  }

  cancelAppointment(id: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.API}/appointments/${id}`);
  }

  getMeasurements(childId: string): Observable<GrowthMeasurement[]> {
    return this.http.get<GrowthMeasurement[]>(`${this.API}/children/${childId}/measurements`);
  }

  addMeasurement(childId: string, measurement: Partial<GrowthMeasurement>): Observable<GrowthMeasurement> {
    return this.http.post<GrowthMeasurement>(`${this.API}/children/${childId}/measurements`, measurement);
  }

  updateMeasurement(childId: string, measurementId: string, measurement: Partial<GrowthMeasurement>): Observable<GrowthMeasurement> {
    return this.http.put<GrowthMeasurement>(`${this.API}/children/${childId}/measurements/${measurementId}`, measurement);
  }

  deleteMeasurement(childId: string, measurementId: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.API}/children/${childId}/measurements/${measurementId}`);
  }

  getRestrictions(childId: string): Observable<DietaryRestriction[]> {
    return this.http.get<DietaryRestriction[]>(`${this.API}/children/${childId}/restrictions`);
  }

  addRestriction(childId: string, restriction: Partial<DietaryRestriction>): Observable<DietaryRestriction> {
    return this.http.post<DietaryRestriction>(`${this.API}/children/${childId}/restrictions`, restriction);
  }

  updateRestriction(childId: string, restrictionId: string, restriction: Partial<DietaryRestriction>): Observable<DietaryRestriction> {
    return this.http.put<DietaryRestriction>(`${this.API}/children/${childId}/restrictions/${restrictionId}`, restriction);
  }

  deleteRestriction(childId: string, restrictionId: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.API}/children/${childId}/restrictions/${restrictionId}`);
  }

  getChildPlans(childId: string): Observable<NutritionalPlan[]> {
    return this.http.get<NutritionalPlan[]>(`${this.API}/children/${childId}/plans`);
  }
}
