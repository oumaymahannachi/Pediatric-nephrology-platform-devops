import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.scss',
  host: { '[class.nav-collapsed]': 'navCollapsed' }
})
export class AdminLayoutComponent {
  currentUser$ = this.auth.currentUser$;
  navCollapsed = false;

  constructor(private auth: AuthService) {}

  toggleNav(): void { this.navCollapsed = !this.navCollapsed; }
  logout(): void { this.auth.logout(); }
}
