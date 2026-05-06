import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BloodTest, LabResult, MedicalImaging } from '../models/examination.model';

@Injectable({ providedIn: 'root' })
export class ExaminationService {
  private readonly API = `${environment.apiUrl}/examinations`;

  constructor(private http: HttpClient) {}

  // Blood Tests
  createBloodTest(bloodTest: Partial<BloodTest>): Observable<BloodTest> {
    return this.http.post<BloodTest>(`${this.API}/blood-tests`, bloodTest);
  }

  getBloodTestsByPatient(patientId: string): Observable<BloodTest[]> {
    return this.http.get<BloodTest[]>(`${this.API}/blood-tests/patient/${patientId}`);
  }

  getBloodTest(id: string): Observable<BloodTest> {
    return this.http.get<BloodTest>(`${this.API}/blood-tests/${id}`);
  }

  updateBloodTest(id: string, bloodTest: Partial<BloodTest>): Observable<BloodTest> {
    return this.http.put<BloodTest>(`${this.API}/blood-tests/${id}`, bloodTest);
  }

  deleteBloodTest(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API}/blood-tests/${id}`);
  }

  // Lab Results
  createLabResult(labResult: Partial<LabResult>): Observable<LabResult> {
    return this.http.post<LabResult>(`${this.API}/lab-results`, labResult);
  }

  getLabResultsByPatient(patientId: string): Observable<LabResult[]> {
    return this.http.get<LabResult[]>(`${this.API}/lab-results/patient/${patientId}`);
  }

  getLabResult(id: string): Observable<LabResult> {
    return this.http.get<LabResult>(`${this.API}/lab-results/${id}`);
  }

  updateLabResult(id: string, labResult: Partial<LabResult>): Observable<LabResult> {
    return this.http.put<LabResult>(`${this.API}/lab-results/${id}`, labResult);
  }

  deleteLabResult(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API}/lab-results/${id}`);
  }

  // Medical Imaging
  createMedicalImaging(imaging: Partial<MedicalImaging>): Observable<MedicalImaging> {
    return this.http.post<MedicalImaging>(`${this.API}/imaging`, imaging);
  }

  getMedicalImagingByPatient(patientId: string): Observable<MedicalImaging[]> {
    return this.http.get<MedicalImaging[]>(`${this.API}/imaging/patient/${patientId}`);
  }

  getMedicalImaging(id: string): Observable<MedicalImaging> {
    return this.http.get<MedicalImaging>(`${this.API}/imaging/${id}`);
  }

  updateMedicalImaging(id: string, imaging: Partial<MedicalImaging>): Observable<MedicalImaging> {
    return this.http.put<MedicalImaging>(`${this.API}/imaging/${id}`, imaging);
  }

  deleteMedicalImaging(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API}/imaging/${id}`);
  }
}
