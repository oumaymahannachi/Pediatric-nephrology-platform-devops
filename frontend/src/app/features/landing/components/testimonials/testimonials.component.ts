import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-testimonials',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './testimonials.component.html',
  styleUrl: './testimonials.component.scss'
})
export class TestimonialsComponent {
  testimonials = [
    {
      name: 'Dr. Sarah Ben Ali',
      role: 'Pediatrician',
      text: 'PediaLink has transformed how I manage my patients. The dashboard is intuitive and saves me hours every week.',
      initials: 'SA'
    },
    {
      name: 'Mohamed Trabelsi',
      role: 'Parent of 2',
      text: 'I can track my children\'s vaccinations and growth easily. The appointment booking feature is a game changer!',
      initials: 'MT'
    },
    {
      name: 'Dr. Amine Khelifi',
      role: 'Clinic Director',
      text: 'We rolled out PediaLink across our entire clinic. Our patient satisfaction scores went up by 40%.',
      initials: 'AK'
    }
  ];
}
