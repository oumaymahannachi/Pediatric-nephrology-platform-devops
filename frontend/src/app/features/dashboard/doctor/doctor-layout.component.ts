import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-doctor-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './doctor-layout.component.html',
  styleUrl: './doctor-layout.component.scss',
  host: { '[class.nav-collapsed]': 'navCollapsed' }
})
export class DoctorLayoutComponent implements OnInit {
  currentUser$ = this.auth.currentUser$;
  navCollapsed = false;
  pendingCount = 0;

  constructor(private auth: AuthService, private doctorService: DoctorService) {}

  ngOnInit(): void {
    this.doctorService.getPendingAppointments().subscribe({
      next: (a) => this.pendingCount = a.length
    });
  }

  toggleNav(): void { this.navCollapsed = !this.navCollapsed; }
  logout(): void { this.auth.logout(); }
}
