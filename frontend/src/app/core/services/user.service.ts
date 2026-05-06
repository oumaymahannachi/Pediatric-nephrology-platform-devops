import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Doctor {
  id: string;
  fullName: string;
  email: string;
  specialization?: string;
  licenseNumber?: string;
  clinicName?: string;
}

export interface Child {
  id: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string;
  parentId: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  getAllDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.API}/users/doctors`);
  }

  getChildrenByParent(parentId: string): Observable<Child[]> {
    return this.http.get<Child[]>(`${this.API}/parent/children`);
  }
}
