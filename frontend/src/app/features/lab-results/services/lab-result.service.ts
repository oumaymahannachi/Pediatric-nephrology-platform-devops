import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { LabResult, CreateLabResultRequest, TestType } from '../models/lab-result.model';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class LabResultService {
  private apiUrl = `${environment.apiUrl}/lab-results`;

  constructor(private http: HttpClient) {}

  createLabResult(request: CreateLabResultRequest): Observable<ApiResponse<LabResult>> {
    return this.http.post<ApiResponse<LabResult>>(this.apiUrl, request);
  }

  getLabResultsByPatient(patientId: string): Observable<ApiResponse<LabResult[]>> {
    return this.http.get<ApiResponse<LabResult[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getLabResultsByPatientAndType(patientId: string, testType: TestType): Observable<ApiResponse<LabResult[]>> {
    return this.http.get<ApiResponse<LabResult[]>>(`${this.apiUrl}/patient/${patientId}/type/${testType}`);
  }

  getAbnormalLabResults(patientId: string): Observable<ApiResponse<LabResult[]>> {
    return this.http.get<ApiResponse<LabResult[]>>(`${this.apiUrl}/patient/${patientId}/abnormal`);
  }

  getLabResultsByDateRange(patientId: string, startDate: string, endDate: string): Observable<ApiResponse<LabResult[]>> {
    return this.http.get<ApiResponse<LabResult[]>>(
      `${this.apiUrl}/patient/${patientId}/date-range?startDate=${startDate}&endDate=${endDate}`
    );
  }

  getLabResultById(id: string): Observable<ApiResponse<LabResult>> {
    return this.http.get<ApiResponse<LabResult>>(`${this.apiUrl}/${id}`);
  }

  updateLabResult(id: string, request: CreateLabResultRequest): Observable<ApiResponse<LabResult>> {
    return this.http.put<ApiResponse<LabResult>>(`${this.apiUrl}/${id}`, request);
  }

  deleteLabResult(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  validateLabResult(id: string): Observable<ApiResponse<LabResult>> {
    return this.http.put<ApiResponse<LabResult>>(`${this.apiUrl}/${id}/validate`, {});
  }
}
