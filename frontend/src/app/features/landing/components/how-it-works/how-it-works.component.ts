import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-how-it-works',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './how-it-works.component.html',
  styleUrl: './how-it-works.component.scss'
})
export class HowItWorksComponent {
  steps = [
    { number: '01', title: 'Create an Account', description: 'Sign up as a Doctor or Parent in less than 2 minutes.' },
    { number: '02', title: 'Set Up Profile', description: 'Complete your profile with medical or family information.' },
    { number: '03', title: 'Connect & Track', description: 'Start tracking health records, scheduling appointments, and communicating.' },
    { number: '04', title: 'Stay Updated', description: 'Receive smart reminders and real-time updates on your child\'s health.' }
  ];
}
