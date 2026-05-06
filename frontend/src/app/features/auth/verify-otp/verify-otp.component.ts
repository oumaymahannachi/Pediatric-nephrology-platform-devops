import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LucideAngularModule],
  templateUrl: './verify-otp.component.html',
  styleUrl: './verify-otp.component.scss'
})
export class VerifyOtpComponent implements OnInit {
  form!: FormGroup;
  email = '';
  purpose = 'VERIFY_EMAIL';
  error = '';
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || '';
      this.purpose = params['purpose'] || 'VERIFY_EMAIL';
    });

    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
    });
  }

  submit() {
    if (this.form.invalid || !this.email) return;

    if (this.purpose === 'RESET_PASSWORD') {
      this.router.navigate(['/auth/forgot-password'], {
        queryParams: { email: this.email, step: 'reset', code: this.form.value.code }
      });
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.verifyOtp({
      email: this.email,
      code: this.form.value.code,
      purpose: this.purpose as 'VERIFY_EMAIL' | 'RESET_PASSWORD'
    }).subscribe({
      next: (res) => {
        this.loading = false;
        this.authService.navigateByRole(res.role);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Invalid or expired OTP.';
      }
    });
  }

  resendOtp() {
    if (!this.email) return;
    this.authService.requestOtp({
      email: this.email,
      purpose: this.purpose as 'VERIFY_EMAIL' | 'RESET_PASSWORD'
    }).subscribe({
      next: () => { this.error = ''; },
      error: (err) => { this.error = err.error?.message || 'Failed to resend OTP.'; }
    });
  }
}
