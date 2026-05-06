import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Prescription, PrescriptionCreateRequest } from '../models/prescription.model';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class PrescriptionService {
  private apiUrl = `${environment.apiUrl}/v1/prescriptions`;

  constructor(private http: HttpClient) {}

  createPrescription(request: PrescriptionCreateRequest): Observable<ApiResponse<Prescription>> {
    return this.http.post<ApiResponse<Prescription>>(`${this.apiUrl}`, request);
  }

  getPrescriptionsByPatient(patientId: string): Observable<ApiResponse<Prescription[]>> {
    return this.http.get<ApiResponse<Prescription[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getPrescription(id: string): Observable<ApiResponse<Prescription>> {
    return this.http.get<ApiResponse<Prescription>>(`${this.apiUrl}/${id}`);
  }

  renewPrescription(id: string): Observable<ApiResponse<Prescription>> {
    return this.http.post<ApiResponse<Prescription>>(`${this.apiUrl}/${id}/renouveler`, {});
  }

  updatePrescription(id: string, request: PrescriptionCreateRequest): Observable<ApiResponse<Prescription>> {
    return this.http.put<ApiResponse<Prescription>>(`${this.apiUrl}/${id}`, request);
  }

  deletePrescription(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  getMyPrescriptions(): Observable<ApiResponse<Prescription[]>> {
    return this.http.get<ApiResponse<Prescription[]>>(`${this.apiUrl}/moi`);
  }
}
