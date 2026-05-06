import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserProfile, ApiMessage } from '../models/auth.models';
import { Child } from '../models/child.model';
import { Appointment } from '../models/appointment.model';
import { GrowthMeasurement, DietaryRestriction, NutritionalPlan } from '../models/treatment.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly API = `${environment.apiUrl}/admin`;
  private readonly TREATMENT_API = `${environment.apiUrl}/treatment-admin`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<any> {
    return this.http.get<any>(`${this.API}/dashboard`);
  }

  getTreatmentDashboard(): Observable<any> {
    return this.http.get<any>(`${this.TREATMENT_API}/dashboard`);
  }

  getAllUsers(): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(`${this.API}/users`);
  }

  getUser(id: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API}/users/${id}`);
  }

  getUsersByRole(role: string): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(`${this.API}/users/role/${role}`);
  }

  banUser(id: string): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(`${this.API}/users/${id}/ban`, {});
  }

  unbanUser(id: string): Observable<ApiMessage> {
    return this.http.put<ApiMessage>(`${this.API}/users/${id}/unban`, {});
  }

  updateUser(id: string, data: Partial<UserProfile>): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.API}/users/${id}`, data);
  }

  deleteUser(id: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.API}/users/${id}`);
  }

  getDoctorPatients(doctorId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API}/doctors/${doctorId}/patients`);
  }

  getAllAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.TREATMENT_API}/appointments`);
  }

  getAllChildren(): Observable<Child[]> {
    return this.http.get<Child[]>(`${this.TREATMENT_API}/children`);
  }

  getAllMeasurements(): Observable<GrowthMeasurement[]> {
    return this.http.get<GrowthMeasurement[]>(`${this.TREATMENT_API}/measurements`);
  }

  getAllRestrictions(): Observable<DietaryRestriction[]> {
    return this.http.get<DietaryRestriction[]>(`${this.TREATMENT_API}/restrictions`);
  }

  getAllNutritionalPlans(): Observable<NutritionalPlan[]> {
    return this.http.get<NutritionalPlan[]>(`${this.TREATMENT_API}/nutritional-plans`);
  }
}
