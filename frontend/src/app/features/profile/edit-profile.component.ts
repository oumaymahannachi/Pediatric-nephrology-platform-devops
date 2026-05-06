import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../core/services/auth.service';
import { UserProfile } from '../../core/models/auth.models';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, LucideAngularModule],
  templateUrl: './edit-profile.component.html',
  styleUrl: './edit-profile.component.scss',
})
export class EditProfileComponent implements OnInit {
  form!: FormGroup;
  profile: UserProfile | null = null;
  loading = true;
  saving = false;
  success = false;
  error = '';
  role = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.role = this.auth.getRole() || '';
    this.buildForm();
    this.loadProfile();
  }

  private buildForm(): void {
    this.form = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: [{ value: '', disabled: true }],
      phone: ['', [Validators.pattern(/^[+]?[\d\s\-()]{7,15}$/)]],
      cin: [{ value: '', disabled: true }],
      specialization: [''],
      licenseNumber: [''],
      clinicName: [''],
      serviceUnit: [''],
    });

    if (this.role === 'DOCTOR') {
      this.form.get('specialization')?.setValidators([Validators.required, Validators.minLength(2)]);
      this.form.get('licenseNumber')?.setValidators([Validators.required, Validators.minLength(3)]);
    }

    if (this.role === 'INFIRMIER') {
      this.form.get('serviceUnit')?.setValidators([Validators.required, Validators.minLength(2)]);
    }
  }

  private loadProfile(): void {
    this.auth.getProfile().subscribe({
      next: (p) => {
        this.profile = p;
        this.form.patchValue({
          fullName: p.fullName || '',
          email: p.email || '',
          phone: p.phone || '',
          cin: p.cin || '',
          specialization: p.specialization || '',
          licenseNumber: p.licenseNumber || '',
          clinicName: p.clinicName || '',
          serviceUnit: p.serviceUnit || '',
        });
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load profile';
        this.loading = false;
      },
    });
  }

  get f() {
    return this.form.controls;
  }

  getInitials(): string {
    const name = this.profile?.fullName || '';
    return name
      .split(' ')
      .map((w) => w.charAt(0))
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getRoleBadge(): string {
    switch (this.role) {
      case 'ADMIN': return 'Administrator';
      case 'DOCTOR': return 'Doctor';
      case 'PARENT': return 'Parent';
      case 'INFIRMIER': return 'Nurse';
      default: return 'User';
    }
  }

  getRoleIcon(): string {
    switch (this.role) {
      case 'ADMIN': return 'shield';
      case 'DOCTOR': return 'stethoscope';
      case 'PARENT': return 'baby';
      case 'INFIRMIER': return 'heart-pulse';
      default: return 'user';
    }
  }

  getBackRoute(): string {
    switch (this.role) {
      case 'ADMIN': return '/admin';
      case 'DOCTOR': return '/doctor';
      case 'PARENT': return '/parent';
      case 'INFIRMIER': return '/infirmier';
      default: return '/';
    }
  }

  save(): void {
    if (this.form.invalid) {
      Object.keys(this.form.controls).forEach((k) => this.form.get(k)?.markAsTouched());
      return;
    }

    this.saving = true;
    this.success = false;
    this.error = '';

    const data: Partial<UserProfile> = {
      fullName: this.form.value.fullName,
      phone: this.form.value.phone || '',
    };

    if (this.role === 'DOCTOR') {
      data.specialization = this.form.value.specialization;
      data.licenseNumber = this.form.value.licenseNumber;
      data.clinicName = this.form.value.clinicName || '';
    }

    if (this.role === 'INFIRMIER') {
      data.serviceUnit = this.form.value.serviceUnit;
    }

    this.auth.updateProfile(data).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.success = true;
        this.saving = false;
        setTimeout(() => (this.success = false), 4000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to save changes';
        this.saving = false;
      },
    });
  }
}
