import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Examen, ExamenCreateRequest } from '../models/examen.model';

@Injectable({
  providedIn: 'root'
})
export class ExamenService {
  private apiUrl = `${environment.apiUrl}/examens`;

  constructor(private http: HttpClient) {}

  getExamensByPatient(patientId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}`);
  }

  getExamenById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  createExamen(examen: ExamenCreateRequest): Observable<any> {
    return this.http.post<any>(this.apiUrl, examen);
  }

  updateExamen(id: string, examen: ExamenCreateRequest): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, examen);
  }

  deleteExamen(id: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  getEvolutionParametre(patientId: string, parametre: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}/evolution/${parametre}`);
  }
}
