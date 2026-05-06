import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Dialyse, DialyseCreateRequest } from '../models/dialyse.model';

@Injectable({
  providedIn: 'root'
})
export class DialyseService {
  private apiUrl = `${environment.apiUrl}/dialyses`;

  constructor(private http: HttpClient) {}

  getDialysesByPatient(patientId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}`);
  }

  getDialyseById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  createDialyse(dialyse: DialyseCreateRequest): Observable<any> {
    return this.http.post<any>(this.apiUrl, dialyse);
  }

  updateDialyse(id: string, dialyse: DialyseCreateRequest): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, dialyse);
  }

  deleteDialyse(id: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  getStatistiques(patientId: string, dateDebut: string, dateFin: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}/statistiques`, {
      params: { dateDebut, dateFin }
    });
  }
}
