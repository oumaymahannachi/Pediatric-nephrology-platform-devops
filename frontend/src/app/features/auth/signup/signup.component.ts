import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink,
    LucideAngularModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.scss'
})
export class SignupComponent {
  step: 'role' | 'form' = 'role';
  selectedRole = '';
  form!: FormGroup;
  error = '';
  loading = false;
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  selectRole(role: string) {
    this.selectedRole = role;
    this.step = 'form';
    this.buildForm();
  }

  private buildForm() {
    const base = {
      fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/)]],
      phone: ['', [Validators.pattern(/^\+?[0-9\s\-]{8,15}$/)]],
      cin: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]]
    };

    if (this.selectedRole === 'DOCTOR') {
      this.form = this.fb.group({
        ...base,
        specialization: ['', Validators.required],
        licenseNumber: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9\-]{3,20}$/)]],
        clinicName: ['']
      });
    } else if (this.selectedRole === 'INFIRMIER') {
      this.form = this.fb.group({
        ...base,
        serviceUnit: ['', Validators.required]
      });
    } else {
      this.form = this.fb.group(base);
    }
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  goBack() {
    this.step = 'role';
    this.error = '';
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';

    const raw = { ...this.form.value, role: this.selectedRole };
    const data = Object.fromEntries(
      Object.entries(raw).filter(([_, v]) => v !== '' && v != null)
    );

    this.authService.signup(data as any).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/auth/verify-otp'], {
          queryParams: { email: data['email'], purpose: 'VERIFY_EMAIL' }
        });
      },
      error: (err) => {
        this.loading = false;
        if (err.error && typeof err.error === 'object' && !err.error.message) {
          // Validation error map from backend
          this.error = Object.values(err.error).join('. ');
        } else {
          this.error = err.error?.message || 'Signup failed. Please try again.';
        }
      }
    });
  }
}
