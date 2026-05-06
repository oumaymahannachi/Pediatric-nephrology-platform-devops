import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PrescriptionService } from '../services/prescription.service';
import { AiService } from '../services/ai.service';
import { Prescription, StatutPrescription } from '../models/prescription.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ChatModalComponent } from '../../messaging/components/chat-modal/chat-modal.component';
import { SenderType } from '../../messaging/models/message.model';
import { ContextType } from '../../messaging/models/conversation.model';

interface Child {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-parent-prescription-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ChatModalComponent],
  templateUrl: './parent-prescription-list.component.html',
  styleUrls: ['./parent-prescription-list.component.css']
})
export class ParentPrescriptionListComponent implements OnInit {
  prescriptions: Prescription[] = [];
  filteredPrescriptions: Prescription[] = [];
  children: Child[] = [];
  loading = false;
  loadingChildren = false;
  error: string | null = null;
  selectedChildId: string | null = null;
  
  // Search and sort
  searchTerm = '';
  sortBy: 'date' | 'status' | 'diagnostic' = 'date';
  sortOrder: 'asc' | 'desc' = 'desc';

  // Translation
  showTranslationModal = false;
  translatedPrescription: any = null;
  translatingPrescription = false;
  translationError: string | null = null;
  selectedLanguage = 'fr';
  availableLanguages = [
    { code: 'fr', name: 'French' },
    { code: 'es', name: 'Spanish' },
    { code: 'de', name: 'German' },
    { code: 'it', name: 'Italian' },
    { code: 'ar', name: 'Arabic' }
  ];

  // Pharmacy locator
  showPharmacyModal = false;
  pharmacies: any[] = [];
  loadingPharmacies = false;
  pharmacyError: string | null = null;

  // Medication Guide
  showGuideModal = false;
  selectedPrescriptionGuide: any = null;
  selectedMedication: any = null;
  selectedMedicationIndex = 0;
  loadingGuide = false;

  // AI Interpretation
  showInterpretationModal = false;
  interpretation: any = null;
  loadingInterpretation = false;
  interpretationError: string | null = null;
  selectedInterpretationLanguage = 'fr'; // Default to French
  interpretationLanguages = [
    { code: 'en', name: 'English' },
    { code: 'fr', name: 'Français' },
    { code: 'ar', name: 'العربية' },
    { code: 'es', name: 'Español' },
    { code: 'de', name: 'Deutsch' }
  ];
  currentPrescriptionForInterpretation: Prescription | null = null;

  // Chat properties
  showChatModal = false;
  selectedPrescriptionForChat: Prescription | null = null;
  currentUserId = localStorage.getItem('userId') || '';
  currentUserType = SenderType.PARENT;
  ContextType = ContextType;

  constructor(
    private prescriptionService: PrescriptionService,
    private aiService: AiService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    this.selectedChildId = navigation?.extras?.state?.['childId'];
    
    this.loadChildren();
    
    if (this.selectedChildId) {
      this.loadPrescriptions(this.selectedChildId);
    }
  }

  loadChildren(): void {
    this.loadingChildren = true;
    this.http.get<any>(`${environment.apiUrl}/parent/children`).subscribe({
      next: (response) => {
        this.children = response.map((c: any) => ({
          id: c.id,
          fullName: c.fullName,
          dateOfBirth: c.dateOfBirth,
          gender: c.gender
        }));
        this.loadingChildren = false;
      },
      error: (err) => {
        console.error('Error loading children:', err);
        this.loadingChildren = false;
      }
    });
  }

  onChildChange(): void {
    if (this.selectedChildId) {
      this.loadPrescriptions(this.selectedChildId);
    } else {
      this.prescriptions = [];
      this.filteredPrescriptions = [];
    }
  }

  loadPrescriptions(childId: string): void {
    this.loading = true;
    this.error = null;
    
    this.prescriptionService.getPrescriptionsByPatient(childId).subscribe({
      next: (response) => {
        this.prescriptions = response.data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading prescriptions';
        this.loading = false;
        console.error(err);
      }
    });
  }
  
  applyFilters(): void {
    let filtered = [...this.prescriptions];
    
    // Search filter
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(p => 
        p.diagnostic.toLowerCase().includes(term) ||
        p.notes?.toLowerCase().includes(term) ||
        p.medicaments.some(m => 
          m.nomCommercial.toLowerCase().includes(term) ||
          m.dci.toLowerCase().includes(term)
        )
      );
    }
    
    // Sort
    filtered.sort((a, b) => {
      let comparison = 0;
      
      if (this.sortBy === 'date') {
        comparison = new Date(a.datePrescription).getTime() - new Date(b.datePrescription).getTime();
      } else if (this.sortBy === 'status') {
        comparison = a.statut.localeCompare(b.statut);
      } else if (this.sortBy === 'diagnostic') {
        comparison = a.diagnostic.localeCompare(b.diagnostic);
      }
      
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });
    
    this.filteredPrescriptions = filtered;
  }
  
  onSearchChange(): void {
    this.applyFilters();
  }
  
  onSortChange(): void {
    this.applyFilters();
  }
  
  toggleSortOrder(): void {
    this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    this.applyFilters();
  }
  
  clearSearch(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

  getStatutClass(statut: StatutPrescription): string {
    const classes: Record<StatutPrescription, string> = {
      [StatutPrescription.ACTIVE]: 'status-active',
      [StatutPrescription.EXPIREE]: 'status-expired',
      [StatutPrescription.TERMINEE]: 'status-completed',
      [StatutPrescription.ANNULEE]: 'status-cancelled',
      [StatutPrescription.RENOUVELEE]: 'status-renewed'
    };
    return classes[statut] || '';
  }

  getStatutLabel(statut: StatutPrescription): string {
    const labels: Record<StatutPrescription, string> = {
      [StatutPrescription.ACTIVE]: 'Active',
      [StatutPrescription.EXPIREE]: 'Expired',
      [StatutPrescription.TERMINEE]: 'Completed',
      [StatutPrescription.ANNULEE]: 'Cancelled',
      [StatutPrescription.RENOUVELEE]: 'Renewed'
    };
    return labels[statut] || statut;
  }

  goBack(): void {
    this.router.navigate(['/parent']);
  }

  translatePrescription(prescription: Prescription): void {
    this.showTranslationModal = true;
    this.translatingPrescription = true;
    this.translationError = null;
    this.translatedPrescription = null;

    this.aiService.translatePrescription(prescription, this.selectedLanguage).subscribe({
      next: (response: any) => {
        this.translatedPrescription = response.data;
        this.translatingPrescription = false;
      },
      error: (err: any) => {
        console.error('Translation error:', err);
        this.translationError = 'Error translating prescription. Please try again.';
        this.translatingPrescription = false;
      }
    });
  }

  closeTranslationModal(): void {
    this.showTranslationModal = false;
    this.translatedPrescription = null;
    this.translationError = null;
  }

  onLanguageChange(prescription: Prescription): void {
    if (this.showTranslationModal) {
      this.translatePrescription(prescription);
    }
  }

  findNearbyPharmacies(): void {
    this.showPharmacyModal = true;
    this.loadingPharmacies = true;
    this.pharmacyError = null;
    this.pharmacies = [];

    // Get user's current location
    if (!navigator.geolocation) {
      this.pharmacyError = 'Geolocation is not supported by your browser';
      this.loadingPharmacies = false;
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const latitude = position.coords.latitude;
        const longitude = position.coords.longitude;

        // Generate simulated pharmacies near user location
        this.pharmacies = this.generateNearbyPharmacies(latitude, longitude);
        this.loadingPharmacies = false;
      },
      (error) => {
        console.error('Geolocation error:', error);
        this.pharmacyError = 'Unable to get your location. Please enable location services.';
        this.loadingPharmacies = false;
      }
    );
  }

  generateNearbyPharmacies(lat: number, lon: number): any[] {
    // Generate 5 simulated pharmacies near user location
    const pharmacies = [
      {
        name: 'Pharmacie Centrale',
        address: '123 Avenue Principale',
        city: 'Tunis',
        postcode: '1000',
        latitude: lat + 0.005,
        longitude: lon + 0.005,
        distance: 0.8
      },
      {
        name: 'Pharmacie de la Santé',
        address: '456 Rue de la République',
        city: 'Tunis',
        postcode: '1001',
        latitude: lat + 0.01,
        longitude: lon - 0.005,
        distance: 1.5
      },
      {
        name: 'Pharmacie du Nord',
        address: '789 Boulevard Habib Bourguiba',
        city: 'Tunis',
        postcode: '1002',
        latitude: lat - 0.008,
        longitude: lon + 0.012,
        distance: 2.3
      },
      {
        name: 'Pharmacie Moderne',
        address: '321 Avenue de la Liberté',
        city: 'Tunis',
        postcode: '1003',
        latitude: lat + 0.015,
        longitude: lon + 0.008,
        distance: 3.1
      },
      {
        name: 'Pharmacie de Nuit',
        address: '654 Rue Mohamed V',
        city: 'Tunis',
        postcode: '1004',
        latitude: lat - 0.012,
        longitude: lon - 0.015,
        distance: 4.2
      }
    ];

    return pharmacies;
  }

  closePharmacyModal(): void {
    this.showPharmacyModal = false;
    this.pharmacies = [];
    this.pharmacyError = null;
  }

  showMedicationGuide(prescription: Prescription): void {
    this.showGuideModal = true;
    this.selectedPrescriptionGuide = prescription;
    this.selectedMedicationIndex = 0;
    this.selectedMedication = prescription.medicaments[0];
    this.loadingGuide = false;
  }

  selectMedication(index: number): void {
    this.selectedMedicationIndex = index;
    this.selectedMedication = this.selectedPrescriptionGuide.medicaments[index];
  }

  getMedicationPurpose(medication: any): string {
    // Simplified explanation based on medication type
    const purposes: any = {
      'default': `Ce médicament aide à traiter ${this.selectedPrescriptionGuide.diagnostic}. Il agit en aidant le corps de votre enfant à mieux fonctionner et à se sentir mieux.`
    };

    return purposes['default'];
  }

  closeGuideModal(): void {
    this.showGuideModal = false;
    this.selectedPrescriptionGuide = null;
    this.selectedMedication = null;
    this.selectedMedicationIndex = 0;
  }

  showAIInterpretation(prescription: Prescription): void {
    this.showInterpretationModal = true;
    this.currentPrescriptionForInterpretation = prescription;
    this.loadInterpretation();
  }

  loadInterpretation(): void {
    if (!this.currentPrescriptionForInterpretation) return;
    
    this.loadingInterpretation = true;
    this.interpretationError = null;
    this.interpretation = null;

    this.aiService.interpretPrescription(
      this.currentPrescriptionForInterpretation.id, 
      this.selectedInterpretationLanguage
    ).subscribe({
      next: (response: any) => {
        this.interpretation = response.data;
        this.loadingInterpretation = false;
      },
      error: (err: any) => {
        console.error('Interpretation error:', err);
        this.interpretationError = 'Error generating interpretation. Please try again.';
        this.loadingInterpretation = false;
      }
    });
  }

  onInterpretationLanguageChange(): void {
    if (this.showInterpretationModal && this.currentPrescriptionForInterpretation) {
      this.loadInterpretation();
    }
  }

  closeInterpretationModal(): void {
    this.showInterpretationModal = false;
    this.interpretation = null;
    this.interpretationError = null;
    this.currentPrescriptionForInterpretation = null;
  }

  // Chat methods
  openChatAboutPrescription(prescription: Prescription): void {
    this.selectedPrescriptionForChat = prescription;
    this.showChatModal = true;
  }

  closeChat(): void {
    this.showChatModal = false;
    this.selectedPrescriptionForChat = null;
  }
}
