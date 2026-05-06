export interface AuthResponse {
  token: string;
  id: string;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'DOCTOR' | 'PARENT' | 'INFIRMIER';
}

export interface SignupRequest {
  fullName: string;
  email: string;
  password: string;
  role: string;
  phone?: string;
  cin: string;
  specialization?: string;
  licenseNumber?: string;
  clinicName?: string;
  serviceUnit?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface OtpRequest {
  email: string;
  purpose: 'VERIFY_EMAIL' | 'RESET_PASSWORD';
}

export interface VerifyOtpRequest {
  email: string;
  code: string;
  purpose: 'VERIFY_EMAIL' | 'RESET_PASSWORD';
}

export interface ResetPasswordRequest {
  email: string;
  code: string;
  newPassword: string;
}

export interface ApiMessage {
  message: string;
}

export interface UserProfile {
  id: string;
  fullName: string;
  email: string;
  role: string;
  phone: string;
  cin: string;
  status: string;
  emailVerified: boolean;
  specialization: string;
  licenseNumber: string;
  clinicName: string;
  serviceUnit: string;
}
