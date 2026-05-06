import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './faq.component.html',
  styleUrl: './faq.component.scss'
})
export class FaqComponent {
  faqs = [
    {
      question: 'Is PediaLink free for parents?',
      answer: 'Yes! Parents can use PediaLink completely free with basic features. Our Free plan includes one child profile, growth tracking, and appointment booking.',
      open: false
    },
    {
      question: 'How do doctors sign up?',
      answer: 'Doctors can create an account by selecting "I am a Doctor" during registration. You\'ll need to provide your license number and specialization for verification.',
      open: false
    },
    {
      question: 'Is my child\'s data secure?',
      answer: 'Absolutely. We use industry-standard encryption and follow best practices for medical data security. All data is encrypted at rest and in transit.',
      open: false
    },
    {
      question: 'Can I track multiple children?',
      answer: 'Yes! With our Free plan you get one child profile. Upgrade to Clinic or Enterprise for unlimited profiles.',
      open: false
    },
    {
      question: 'Do you offer a mobile app?',
      answer: 'Our web platform is fully responsive and works great on mobile devices. Native iOS and Android apps are on our roadmap.',
      open: false
    },
    {
      question: 'How does appointment booking work?',
      answer: 'Parents can view their doctor\'s availability and book appointments directly. Doctors receive notifications and can manage their schedule in real-time.',
      open: false
    }
  ];

  toggle(faq: { open: boolean }) {
    faq.open = !faq.open;
  }
}
