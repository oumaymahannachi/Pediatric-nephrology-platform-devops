import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Treatment, TreatmentCreateRequest } from '../models/treatment.model';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class TreatmentService {
  private apiUrl = `${environment.apiUrl}/v1/traitements`;

  constructor(private http: HttpClient) {}

  createTreatment(request: TreatmentCreateRequest): Observable<ApiResponse<Treatment>> {
    return this.http.post<ApiResponse<Treatment>>(`${this.apiUrl}`, request);
  }

  getTreatmentsByPatient(patientId: string): Observable<ApiResponse<Treatment[]>> {
    return this.http.get<ApiResponse<Treatment[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getTreatment(id: string): Observable<ApiResponse<Treatment>> {
    return this.http.get<ApiResponse<Treatment>>(`${this.apiUrl}/${id}`);
  }

  updateTreatment(id: string, request: TreatmentCreateRequest): Observable<ApiResponse<Treatment>> {
    return this.http.put<ApiResponse<Treatment>>(`${this.apiUrl}/${id}`, request);
  }

  deleteTreatment(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
