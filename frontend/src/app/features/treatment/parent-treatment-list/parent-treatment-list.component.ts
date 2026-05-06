import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TreatmentService } from '../services/treatment.service';
import { TreatmentAiService } from '../services/treatment-ai.service';
import { Treatment, StatutTraitement } from '../models/treatment.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ChatModalComponent } from '../../messaging/components/chat-modal/chat-modal.component';
import { SenderType } from '../../messaging/models/message.model';
import { ContextType } from '../../messaging/models/conversation.model';

declare var L: any;

interface Child {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
}

@Component({
  selector: 'app-parent-treatment-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ChatModalComponent],
  templateUrl: './parent-treatment-list.component.html',
  styleUrls: ['./parent-treatment-list.component.css']
})
export class ParentTreatmentListComponent implements OnInit {
  @ViewChild('mapElement', { static: false }) mapElement!: ElementRef;
  
  treatments: Treatment[] = [];
  children: Child[] = [];
  loading = false;
  loadingChildren = false;
  error: string | null = null;
  selectedChildId: string | null = null;

  showTranslationModal = false;
  translatedTreatment: any = null;
  translatingTreatment = false;
  translationError: string | null = null;
  selectedLanguage = 'fr';
  availableLanguages = [
    { code: 'fr', name: 'French' },
    { code: 'es', name: 'Spanish' },
    { code: 'de', name: 'German' },
    { code: 'it', name: 'Italian' },
    { code: 'ar', name: 'Arabic' }
  ];

  showPharmacyModal = false;
  pharmacies: any[] = [];
  loadingPharmacies = false;
  pharmacyError: string | null = null;
  map: any;
  markers: any[] = [];
  selectedPharmacyIndex: number = -1;
  userLocation: { lat: number; lng: number } | null = null;

  showChatModal = false;
  selectedTreatmentForChat: Treatment | null = null;
  currentUserId = localStorage.getItem('userId') || '';
  currentUserType = SenderType.PARENT;
  ContextType = ContextType;

  constructor(
    private treatmentService: TreatmentService,
    private aiService: TreatmentAiService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadChildren();
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
      this.loadTreatments(this.selectedChildId);
    } else {
      this.treatments = [];
    }
  }

  loadTreatments(childId: string): void {
    this.loading = true;
    this.error = null;
    
    this.treatmentService.getTreatmentsByPatient(childId).subscribe({
      next: (response) => {
        this.treatments = response.data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des traitements';
        this.loading = false;
        console.error(err);
      }
    });
  }

  getStatutClass(statut: StatutTraitement): string {
    const classes: Record<StatutTraitement, string> = {
      [StatutTraitement.EN_COURS]: 'status-active',
      [StatutTraitement.TERMINE]: 'status-completed',
      [StatutTraitement.SUSPENDU]: 'status-suspended',
      [StatutTraitement.ANNULE]: 'status-cancelled'
    };
    return classes[statut] || '';
  }

  getStatutLabel(statut: StatutTraitement): string {
    const labels: Record<StatutTraitement, string> = {
      [StatutTraitement.EN_COURS]: 'En cours',
      [StatutTraitement.TERMINE]: 'Terminé',
      [StatutTraitement.SUSPENDU]: 'Suspendu',
      [StatutTraitement.ANNULE]: 'Annulé'
    };
    return labels[statut] || statut;
  }

  translateTreatment(treatment: Treatment): void {
    this.showTranslationModal = true;
    this.translatingTreatment = true;
    this.translationError = null;
    this.translatedTreatment = null;

    this.aiService.translateTreatment(treatment, this.selectedLanguage).subscribe({
      next: (response: any) => {
        this.translatedTreatment = response.data;
        this.translatingTreatment = false;
      },
      error: (err: any) => {
        console.error('Translation error:', err);
        this.translationError = 'Erreur lors de la traduction. Veuillez réessayer.';
        this.translatingTreatment = false;
      }
    });
  }

  closeTranslationModal(): void {
    this.showTranslationModal = false;
    this.translatedTreatment = null;
    this.translationError = null;
  }

  onLanguageChange(treatment: Treatment): void {
    if (this.showTranslationModal) {
      this.translateTreatment(treatment);
    }
  }

  findNearbyPharmacies(): void {
    this.showPharmacyModal = true;
    this.loadingPharmacies = true;
    this.pharmacyError = null;
    this.pharmacies = [];
    this.selectedPharmacyIndex = -1;

    if (!navigator.geolocation) {
      this.pharmacyError = 'La géolocalisation n\'est pas supportée par votre navigateur';
      this.loadingPharmacies = false;
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.userLocation = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        };

        this.pharmacies = this.generateNearbyPharmacies(this.userLocation.lat, this.userLocation.lng);
        this.loadingPharmacies = false;

        setTimeout(() => this.initializeMap(), 100);
      },
      (error) => {
        console.error('Geolocation error:', error);
        this.pharmacyError = 'Impossible d\'obtenir votre position. Veuillez activer la géolocalisation.';
        this.loadingPharmacies = false;
      }
    );
  }

  generateNearbyPharmacies(lat: number, lon: number): any[] {
    const pharmacies = [
      {
        name: 'Pharmacie Centrale',
        address: '123 Avenue Principale, Tunis',
        latitude: lat + 0.005,
        longitude: lon + 0.005,
        distance: 0.8
      },
      {
        name: 'Pharmacie de la Santé',
        address: '456 Rue de la République, Tunis',
        latitude: lat + 0.01,
        longitude: lon - 0.005,
        distance: 1.5
      },
      {
        name: 'Pharmacie du Nord',
        address: '789 Boulevard Habib Bourguiba, Tunis',
        latitude: lat - 0.008,
        longitude: lon + 0.012,
        distance: 2.3
      },
      {
        name: 'Pharmacie Moderne',
        address: '321 Avenue de la Liberté, Tunis',
        latitude: lat + 0.015,
        longitude: lon + 0.008,
        distance: 3.1
      },
      {
        name: 'Pharmacie de Nuit',
        address: '654 Rue Mohamed V, Tunis',
        latitude: lat - 0.012,
        longitude: lon - 0.015,
        distance: 4.2
      }
    ];

    return pharmacies;
  }

  initializeMap(): void {
    if (!this.mapElement || !this.userLocation) return;

    try {
      this.map = L.map(this.mapElement.nativeElement).setView(
        [this.userLocation.lat, this.userLocation.lng], 
        13
      );

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
      }).addTo(this.map);

      const userIcon = L.divIcon({
        className: 'user-marker',
        html: '<div style="background: #4285F4; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 8px rgba(0,0,0,0.3);"></div>',
        iconSize: [20, 20],
        iconAnchor: [10, 10]
      });

      L.marker([this.userLocation.lat, this.userLocation.lng], { icon: userIcon })
        .addTo(this.map)
        .bindPopup('Votre position');

      this.markers = this.pharmacies.map((pharmacy, index) => {
        const pharmacyIcon = L.divIcon({
          className: 'pharmacy-marker',
          html: `<div style="background: #10b981; color: white; width: 30px; height: 30px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 8px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 14px;">${index + 1}</div>`,
          iconSize: [30, 30],
          iconAnchor: [15, 15]
        });

        const marker = L.marker([pharmacy.latitude, pharmacy.longitude], { icon: pharmacyIcon })
          .addTo(this.map)
          .bindPopup(`<b>${pharmacy.name}</b><br>${pharmacy.address}<br><small>${pharmacy.distance} km</small>`);

        marker.on('click', () => {
          this.selectPharmacy(index);
        });

        return marker;
      });

    } catch (error) {
      console.error('Error initializing map:', error);
      this.pharmacyError = 'Erreur lors du chargement de la carte';
    }
  }

  selectPharmacy(index: number): void {
    this.selectedPharmacyIndex = index;
    const pharmacy = this.pharmacies[index];

    if (this.map) {
      this.map.setView([pharmacy.latitude, pharmacy.longitude], 15, {
        animate: true,
        duration: 0.5
      });

      if (this.markers[index]) {
        this.markers[index].openPopup();
      }
    }
  }

  closePharmacyModal(): void {
    this.showPharmacyModal = false;
    this.pharmacies = [];
    this.pharmacyError = null;
    this.map = null;
    this.markers = [];
    this.selectedPharmacyIndex = -1;
    this.userLocation = null;
  }

  openChatAboutTreatment(treatment: Treatment): void {
    this.selectedTreatmentForChat = treatment;
    this.showChatModal = true;
  }

  closeChat(): void {
    this.showChatModal = false;
    this.selectedTreatmentForChat = null;
  }
}
