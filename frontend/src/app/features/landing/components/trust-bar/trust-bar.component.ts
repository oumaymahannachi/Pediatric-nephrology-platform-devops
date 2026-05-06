import { Component } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-trust-bar',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './trust-bar.component.html',
  styleUrl: './trust-bar.component.scss'
})
export class TrustBarComponent {}
