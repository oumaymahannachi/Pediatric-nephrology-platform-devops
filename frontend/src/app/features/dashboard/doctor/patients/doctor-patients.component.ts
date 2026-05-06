import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { DoctorService } from '../../../../core/services/doctor.service';
import { Child } from '../../../../core/models/child.model';

@Component({
  selector: 'app-doctor-patients',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './doctor-patients.component.html',
  styleUrl: './doctor-patients.component.scss'
})
export class DoctorPatientsComponent implements OnInit {
  loading = true;
  patients: Child[] = [];

  constructor(private doctorService: DoctorService) {}

  ngOnInit(): void {
    this.doctorService.getPatients().subscribe({
      next: (p) => { this.patients = p; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
