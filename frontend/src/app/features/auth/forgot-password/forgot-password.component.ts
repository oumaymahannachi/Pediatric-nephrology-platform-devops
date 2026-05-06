import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LucideAngularModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent implements OnInit {
  step: 'email' | 'reset' = 'email';
  emailForm!: FormGroup;
  resetForm!: FormGroup;
  error = '';
  success = '';
  loading = false;
  email = '';
  code = '';
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/)]],
      confirmPassword: ['', [Validators.required]]
    });

    this.route.queryParams.subscribe(params => {
      if (params['step'] === 'reset') {
        this.step = 'reset';
        this.email = params['email'] || '';
        this.code = params['code'] || '';
      }
    });
  }

  requestReset() {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';

    const email = this.emailForm.value.email;

    this.authService.requestOtp({ email, purpose: 'RESET_PASSWORD' }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/auth/verify-otp'], {
          queryParams: { email, purpose: 'RESET_PASSWORD' }
        });
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to send reset code.';
      }
    });
  }

  resetPassword() {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }
    const { newPassword, confirmPassword } = this.resetForm.value;

    if (newPassword !== confirmPassword) {
      this.error = 'Passwords do not match';
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.resetPassword({
      email: this.email,
      code: this.code,
      newPassword
    }).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Password reset successfully! Redirecting to login...';
        setTimeout(() => this.router.navigate(['/auth/login']), 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to reset password.';
      }
    });
  }
}
