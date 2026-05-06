import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './features.component.html',
  styleUrl: './features.component.scss'
})
export class FeaturesComponent {
  features = [
    {
      icon: 'stethoscope',
      title: 'Doctor Portal',
      description: 'Manage patients, view medical records, and schedule appointments from a powerful dashboard.'
    },
    {
      icon: 'baby',
      title: 'Parent Portal',
      description: 'Track your child\'s growth, vaccination schedules, and communicate with your pediatrician.'
    },
    {
      icon: 'shield',
      title: 'Secure Medical Tracking',
      description: 'All health data is encrypted and securely stored. HIPAA-compliant architecture.'
    },
    {
      icon: 'calendar',
      title: 'Smart Appointments',
      description: 'Book, reschedule, or cancel appointments with real-time availability.'
    },
    {
      icon: 'bar-chart-3',
      title: 'Growth Analytics',
      description: 'Visual growth charts and developmental milestone tracking for each child.'
    },
    {
      icon: 'bell',
      title: 'Smart Notifications',
      description: 'Automated reminders for vaccinations, check-ups, and medication schedules.'
    }
  ];
}
