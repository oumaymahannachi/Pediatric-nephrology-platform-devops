import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConsultationService } from '../services/consultation.service';
import { ParentService } from '../../../core/services/parent.service';
import { LucideAngularModule } from 'lucide-angular';
import { Child } from '../../../core/models/child.model';
import { Consultation } from '../models/consultation.model';

@Component({
  selector: 'app-parent-consultations',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, LucideAngularModule],
  template: `
    <div class="pc">
      <div class="pc__header">
        <h1><lucide-icon name="calendar" [size]="24"></lucide-icon> Mes Consultations</h1>
        <button class="pc__btn pc__btn--primary" (click)="openRequestModal()">
          <lucide-icon name="plus" [size]="18"></lucide-icon> Demander une consultation
        </button>
      </div>

      <div class="pc__tabs">
        <button 
          *ngFor="let child of children" 
          class="pc__tab" 
          [class.active]="selectedChildId === child.id"
          (click)="selectChild(child.id!)">
          {{ child.fullName }}
        </button>
      </div>

      <div class="pc__content">
        <div *ngIf="loading" class="pc__loading">
          <div class="pc__spinner"></div>
          Chargement des consultations...
        </div>

        <div *ngIf="!loading && consultations.length === 0" class="pc__empty">
          <lucide-icon name="calendar-x" [size]="48"></lucide-icon>
          <h3>Aucune consultation trouvée</h3>
          <p>Vous n'avez pas encore de consultations ou de demandes pour cet enfant.</p>
        </div>

        <div *ngIf="!loading && consultations.length > 0" class="pc__grid">
          <div *ngFor="let c of consultations" class="pc__card">
            <div class="pc__card-header">
              <span class="pc__badge" [ngClass]="'pc__badge--' + getStatusClass(c.statut)">
                {{ c.statut }}
              </span>
              <span class="pc__date">{{ c.dateRendezVous | date:'dd/MM/yyyy HH:mm' }}</span>
            </div>
            <div class="pc__card-body">
              <h4>{{ c.motifConsultation }}</h4>
              <p *ngIf="c.observationsCliniques" class="pc__obs">{{ c.observationsCliniques }}</p>
            </div>
            <div class="pc__card-footer" *ngIf="c.statut === 'REFUSEE' && c.dateProposee">
              <div class="pc__proposal">
                <p>Le médecin propose une nouvelle date :</p>
                <strong>{{ c.dateProposee | date:'dd/MM/yyyy HH:mm' }}</strong>
                <button class="pc__btn pc__btn--small" (click)="acceptProposedDate(c.id!)">Accepter cette date</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal de demande -->
      <div *ngIf="showModal" class="pc__modal-overlay" (click)="closeModal()">
        <div class="pc__modal" (click)="$event.stopPropagation()">
          <div class="pc__modal-header">
            <h2>Demander une consultation</h2>
            <button (click)="closeModal()"><lucide-icon name="x" [size]="20"></lucide-icon></button>
          </div>
          <form [formGroup]="requestForm" (ngSubmit)="submitRequest()" class="pc__modal-body">
            <div class="pc__field">
              <label>Enfant *</label>
              <select formControlName="patientId">
                <option value="">Sélectionner un enfant</option>
                <option *ngFor="let child of children" [value]="child.id">{{ child.fullName }}</option>
              </select>
            </div>
            <div class="pc__field">
              <label>Médecin *</label>
              <select formControlName="medecinId">
                <option value="">Sélectionner un médecin</option>
                <option *ngFor="let doc of medecins" [value]="doc.id">Dr. {{ doc.fullName }} - {{ doc.specialization }}</option>
              </select>
            </div>
            <div class="pc__field">
              <label>Motif de la consultation *</label>
              <textarea formControlName="motifConsultation" rows="3" placeholder="Décrivez le motif..."></textarea>
            </div>
            <div class="pc__field">
              <label>Date souhaitée *</label>
              <input type="datetime-local" formControlName="dateRendezVous" />
            </div>
            <div class="pc__modal-actions">
              <button type="button" class="pc__btn pc__btn--outline" (click)="closeModal()">Annuler</button>
              <button type="submit" class="pc__btn pc__btn--primary" [disabled]="requestForm.invalid || submitting">
                {{ submitting ? 'Envoi...' : 'Envoyer la demande' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .pc { padding: 2rem; max-width: 1200px; margin: 0 auto; font-family: 'Inter', sans-serif; }
    .pc__header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
    .pc__header h1 { display: flex; align-items: center; gap: 0.75rem; font-size: 1.5rem; font-weight: 700; color: #1e293b; margin: 0; }
    
    .pc__tabs { display: flex; gap: 1rem; margin-bottom: 2rem; border-bottom: 1px solid #e2e8f0; padding-bottom: 0.5rem; }
    .pc__tab { padding: 0.5rem 1rem; border: none; background: none; font-weight: 500; color: #64748b; cursor: pointer; border-bottom: 2px solid transparent; transition: all 0.2s; }
    .pc__tab:hover { color: #0ea5e9; }
    .pc__tab.active { color: #0ea5e9; border-bottom-color: #0ea5e9; }

    .pc__grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; }
    .pc__card { background: white; border-radius: 1rem; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
    .pc__card-header { padding: 1rem; background: #f8fafc; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
    .pc__badge { padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; }
    .pc__badge--demandee { background: #fef3c7; color: #92400e; }
    .pc__badge--planifiee { background: #dcfce7; color: #166534; }
    .pc__badge--refusee { background: #fee2e2; color: #991b1b; }
    
    .pc__card-body { padding: 1rem; }
    .pc__card-body h4 { margin: 0 0 0.5rem; color: #1e293b; }
    .pc__obs { font-size: 0.875rem; color: #64748b; margin: 0; }
    
    .pc__card-footer { padding: 1rem; background: #fff7ed; border-top: 1px solid #ffedd5; }
    .pc__proposal { font-size: 0.875rem; color: #9a3412; }
    .pc__proposal strong { display: block; margin: 0.25rem 0 0.5rem; font-size: 1rem; }

    .pc__btn { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.625rem 1.25rem; border-radius: 0.5rem; font-weight: 600; cursor: pointer; transition: all 0.2s; border: none; }
    .pc__btn--primary { background: #0ea5e9; color: white; }
    .pc__btn--primary:hover { background: #0284c7; transform: translateY(-1px); }
    .pc__btn--outline { background: white; border: 1px solid #e2e8f0; color: #64748b; }
    .pc__btn--small { padding: 0.375rem 0.75rem; font-size: 0.875rem; background: #f97316; color: white; }

    .pc__modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .pc__modal { background: white; border-radius: 1rem; width: 90%; max-width: 500px; padding: 1.5rem; }
    .pc__modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .pc__modal-header h2 { margin: 0; font-size: 1.25rem; }
    .pc__modal-header button { background: none; border: none; cursor: pointer; color: #64748b; }
    
    .pc__field { margin-bottom: 1.25rem; }
    .pc__field label { display: block; font-size: 0.875rem; font-weight: 600; margin-bottom: 0.375rem; color: #475569; }
    .pc__field select, .pc__field input, .pc__field textarea { width: 100%; padding: 0.625rem; border: 1px solid #e2e8f0; border-radius: 0.5rem; font-family: inherit; }
    
    .pc__modal-actions { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 1.5rem; }
    
    .pc__empty { text-align: center; padding: 4rem 2rem; color: #94a3b8; }
    .pc__empty h3 { color: #64748b; margin: 1rem 0 0.5rem; }
    
    .pc__loading { text-align: center; padding: 4rem; color: #64748b; display: flex; flex-direction: column; align-items: center; gap: 1rem; }
    .pc__spinner { width: 2.5rem; height: 2.5rem; border: 3px solid #e2e8f0; border-top-color: #0ea5e9; border-radius: 50%; animation: spin 1s linear infinite; }
    
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class ParentConsultationsComponent implements OnInit {
  children: Child[] = [];
  medecins: any[] = [];
  consultations: Consultation[] = [];
  selectedChildId: string | null = null;
  loading = false;
  submitting = false;
  showModal = false;
  requestForm!: FormGroup;

  constructor(
    private parentService: ParentService,
    private consultationService: ConsultationService,
    private fb: FormBuilder
  ) {
    this.requestForm = this.fb.group({
      patientId: ['', Validators.required],
      medecinId: ['', Validators.required],
      motifConsultation: ['', Validators.required],
      dateRendezVous: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadChildren();
    this.loadDoctors();
  }

  loadChildren(): void {
    this.parentService.getChildren().subscribe({
      next: (children) => {
        this.children = children;
        if (this.children.length > 0) {
          this.selectChild(this.children[0].id!);
        }
      }
    });
  }

  loadDoctors(): void {
    this.parentService.getAvailableDoctors().subscribe({
      next: (docs) => this.medecins = docs
    });
  }

  selectChild(childId: string): void {
    this.selectedChildId = childId;
    this.loadConsultations();
  }

  loadConsultations(): void {
    if (!this.selectedChildId) return;
    this.loading = true;
    this.consultationService.getConsultationsByPatient(this.selectedChildId).subscribe({
      next: (data) => {
        this.consultations = data.data || data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  openRequestModal(): void {
    this.requestForm.reset({ patientId: this.selectedChildId || '', medecinId: '' });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  submitRequest(): void {
    if (this.requestForm.invalid) return;
    this.submitting = true;
    this.consultationService.createConsultationRequest(this.requestForm.value).subscribe({
      next: () => {
        this.submitting = false;
        this.closeModal();
        this.loadConsultations();
      },
      error: () => this.submitting = false
    });
  }

  acceptProposedDate(id: string): void {
    this.consultationService.acceptProposedDate(id).subscribe({
      next: () => this.loadConsultations()
    });
  }

  getStatusClass(status: string): string {
    return (status || '').toLowerCase();
  }
}
