import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule],
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.scss'
})
export class PricingComponent {
  plans = [
    {
      name: 'Free',
      price: '0',
      description: 'Perfect for individual parents',
      features: ['1 Child profile', 'Basic growth tracking', 'Appointment booking', 'Email support'],
      cta: 'Get Started',
      highlighted: false
    },
    {
      name: 'Clinic',
      price: '49',
      description: 'Best for pediatric practices',
      features: ['Unlimited patients', 'Advanced analytics', 'Team collaboration', 'Priority support', 'Custom branding'],
      cta: 'Start Free Trial',
      highlighted: true
    },
    {
      name: 'Enterprise',
      price: 'Custom',
      description: 'For hospitals & networks',
      features: ['Everything in Clinic', 'Multi-location support', 'API access', 'Dedicated account manager', 'SLA guarantee'],
      cta: 'Contact Sales',
      highlighted: false
    }
  ];
}
