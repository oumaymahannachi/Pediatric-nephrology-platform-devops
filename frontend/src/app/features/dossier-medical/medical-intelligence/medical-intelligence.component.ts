import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MedicalIntelligenceService, BiologicalInterpretation, PathologyEvolution, MedicalReminder } from '../../../core/services/medical-intelligence.service';
import { AuthService } from '../../../core/services/auth.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-medical-intelligence',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './medical-intelligence.component.html',
  styleUrls: ['./medical-intelligence.component.css']
})
export class MedicalIntelligenceComponent implements OnChanges {
  @Input() patientId!: string;
  @Input() patientName = '';
  @Input() patientDateOfBirth = '';

  activeTab: 'bio' | 'evolution' | 'reminders' = 'bio';

  ageMonths = 60;
  weightKg = 20;
  bioInterpretation: BiologicalInterpretation | null = null;
  bioLoading = false;

  evolution: PathologyEvolution | null = null;
  evolutionLoading = false;

  reminders: MedicalReminder[] = [];
  remindersLoading = false;

  currentUserId = '';

  constructor(
    private intelligenceService: MedicalIntelligenceService,
    private auth: AuthService
  ) {
    this.auth.currentUser$.subscribe(user => {
      if (user) this.currentUserId = user.id || '';
    });
    if (!this.currentUserId) {
      this.currentUserId = localStorage.getItem('userId') || '';
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['patientId'] && this.patientId) {
      if (this.patientDateOfBirth) {
        this.ageMonths = this.calculateAgeInMonths(this.patientDateOfBirth);
      }
      this.loadAll();
    }
    if (changes['patientDateOfBirth'] && this.patientDateOfBirth) {
      this.ageMonths = this.calculateAgeInMonths(this.patientDateOfBirth);
    }
  }

  private calculateAgeInMonths(dateOfBirth: string): number {
    const birth = new Date(dateOfBirth);
    const now = new Date();
    const months = (now.getFullYear() - birth.getFullYear()) * 12
                 + (now.getMonth() - birth.getMonth());
    return Math.max(1, months);
  }

  loadAll(): void {
    this.loadBioInterpretation();
    this.loadEvolution();
    this.loadReminders();
  }

  loadBioInterpretation(): void {
    if (!this.patientId) return;
    this.bioLoading = true;
    this.intelligenceService.getBiologicalInterpretation(this.patientId, this.ageMonths, this.weightKg)
      .subscribe({
        next: (data) => { this.bioInterpretation = data; this.bioLoading = false; },
        error: () => this.bioLoading = false
      });
  }

  loadEvolution(): void {
    if (!this.patientId) return;
    this.evolutionLoading = true;
    this.intelligenceService.getPathologyEvolution(this.patientId)
      .subscribe({
        next: (data) => { this.evolution = data; this.evolutionLoading = false; },
        error: () => this.evolutionLoading = false
      });
  }

  loadReminders(): void {
    if (!this.patientId) return;
    this.remindersLoading = true;
    this.intelligenceService.getPatientReminders(this.patientId)
      .subscribe({
        next: (data) => { this.reminders = data; this.remindersLoading = false; },
        error: () => this.remindersLoading = false
      });
  }

  generateReminders(): void {
    if (!this.patientId) return;
    const medecinId = this.currentUserId || localStorage.getItem('userId') || 'doctor';
    this.remindersLoading = true;
    this.intelligenceService.generateReminders(this.patientId, medecinId)
      .subscribe({
        next: (data) => {
          console.log('Reminders generated:', data);
          this.reminders = [...this.reminders, ...data];
          this.remindersLoading = false;
        },
        error: (err) => {
          console.error('Error generating reminders:', err);
          this.remindersLoading = false;
        }
      });
  }

  acknowledgeReminder(reminderId: string): void {
    this.intelligenceService.acknowledgeReminder(reminderId).subscribe({
      next: () => {
        this.reminders = this.reminders.map(r =>
          r.id === reminderId ? { ...r, status: 'ACKNOWLEDGED' } : r
        );
      }
    });
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      NORMAL: 'status-normal',
      LOW: 'status-low',
      HIGH: 'status-high',
      CRITICAL_LOW: 'status-critical',
      CRITICAL_HIGH: 'status-critical'
    };
    return map[status] || '';
  }

  getTrendIcon(trend: string): string {
    const map: Record<string, string> = {
      IMPROVING: '↑',
      STABLE: '→',
      WORSENING: '↓',
      RAPID_WORSENING: '↓↓',
      INSUFFICIENT_DATA: '?'
    };
    return map[trend] || '?';
  }

  getTrendClass(trend: string): string {
    const map: Record<string, string> = {
      IMPROVING: 'trend-good',
      STABLE: 'trend-stable',
      WORSENING: 'trend-bad',
      RAPID_WORSENING: 'trend-critical',
      INSUFFICIENT_DATA: 'trend-unknown'
    };
    return map[trend] || '';
  }

  getProgressionClass(progression: string): string {
    const map: Record<string, string> = {
      IMPROVING: 'progression-good',
      STABLE: 'progression-stable',
      WORSENING: 'progression-bad',
      RAPID_WORSENING: 'progression-critical'
    };
    return map[progression] || '';
  }

  getReminderTypeIcon(type: string): string {
    const map: Record<string, string> = {
      APPOINTMENT_REMINDER: '📅',
      MANDATORY_FOLLOWUP: '⚕️',
      INACTIVITY_ALERT: '⚠️',
      EXAM_FOLLOWUP: '🔬',
      DIALYSIS_SESSION: '💉'
    };
    return map[type] || '🔔';
  }

  getPendingReminders(): MedicalReminder[] {
    return this.reminders.filter(r => r.status === 'PENDING');
  }
}
