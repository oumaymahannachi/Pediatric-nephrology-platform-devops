import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-infirmier-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './infirmier-dashboard.component.html',
  styleUrl: './infirmier-dashboard.component.scss',
})
export class InfirmierDashboardComponent {
  currentUser$ = this.auth.currentUser$;

  navItems = [
    { icon: 'layout-dashboard', label: 'Dashboard', active: true },
    { icon: 'baby', label: 'Patients', active: false },
    { icon: 'calendar', label: 'Appointments', active: false },
    { icon: 'clipboard-list', label: 'Care Plans', active: false },
    { icon: 'activity', label: 'Vitals Tracking', active: false },
    { icon: 'settings', label: 'Settings', active: false },
  ];

  stats = [
    { icon: 'baby', label: 'Assigned Patients', value: '\u2013' },
    { icon: 'calendar', label: "Today's Tasks", value: '\u2013' },
    { icon: 'clipboard-list', label: 'Pending Care Plans', value: '\u2013' },
    { icon: 'activity', label: 'Vitals Recorded', value: '\u2013' },
  ];

  constructor(private auth: AuthService) {}

  setActiveNav(item: any): void {
    this.navItems.forEach(n => n.active = false);
    item.active = true;
  }

  logout(): void {
    this.auth.logout();
  }
}
