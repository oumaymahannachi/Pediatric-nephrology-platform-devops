import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  SignupRequest,
  LoginRequest,
  OtpRequest,
  VerifyOtpRequest,
  ResetPasswordRequest,
  ApiMessage,
  UserProfile
} from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'pedialink_token';
  private readonly USER_KEY = 'pedialink_user';

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getSavedUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  signup(data: SignupRequest): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${this.API}/signup`, data);
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/login`, data).pipe(
      tap(res => this.saveSession(res))
    );
  }

  requestOtp(data: OtpRequest): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${this.API}/request-otp`, data);
  }

  verifyOtp(data: VerifyOtpRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/verify-otp`, data).pipe(
      tap(res => this.saveSession(res))
    );
  }

  resetPassword(data: ResetPasswordRequest): Observable<ApiMessage> {
    return this.http.post<ApiMessage>(`${this.API}/reset-password`, data);
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiUrl}/users/me`);
  }

  updateProfile(data: Partial<UserProfile>): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${environment.apiUrl}/users/me`, data).pipe(
      tap(profile => {
        const current = this.currentUserSubject.value;
        if (current) {
          this.saveSession({ ...current, fullName: profile.fullName });
        }
      })
    );
  }

  saveSession(res: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, res.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(res));
    this.currentUserSubject.next(res);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getRole(): string | null {
    const user = this.currentUserSubject.value;
    return user ? user.role : null;
  }

  private getSavedUser(): AuthResponse | null {
    const raw = localStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  navigateByRole(role: string): void {
    switch (role) {
      case 'ADMIN':  this.router.navigate(['/admin']); break;
      case 'DOCTOR': this.router.navigate(['/doctor']); break;
      case 'PARENT': this.router.navigate(['/parent']); break;
      case 'INFIRMIER': this.router.navigate(['/infirmier']); break;
      default:       this.router.navigate(['/']); break;
    }
  }
}
