export interface Prescription {
  id: string;
  patientId: string;
  medecinId: string;
  datePrescription: string;
  dateExpiration: string;
  dureeValiditeJours: number;
  diagnostic: string;
  medicaments: Medicament[];
  notes?: string;
  statut: StatutPrescription;
  renouvelable?: boolean;
  nombreRenouvellementsRestants?: number;
  nombreRenouvellementsEffectues?: number;
  createdAt: string;
  updatedAt: string;
}

export interface Medicament {
  nomCommercial: string;
  dci: string;
  formePharmaceutique?: string;
  dosage?: string;
  posologie: Posologie;
  instructionsSpeciales?: string;
  substitutable?: boolean;
  contreIndications?: string[];
  interactionsConnues?: string[];
}

export interface Posologie {
  quantite: number;
  unite: string;
  frequence: string;
  momentPrise?: string;
  dureeTraitementJours?: number;
  isPediatrique?: boolean;
  poidsPatientKg?: number;
  doseParKg?: number;
  surfaceCorporelleM2?: number;
  doseTotaleCalculee?: number;
  calculDoseDetails?: string;
  ajustementRenal?: boolean;
  ajustementHepatique?: boolean;
  justificationAjustement?: string;
}

export enum StatutPrescription {
  ACTIVE = 'ACTIVE',
  EXPIREE = 'EXPIREE',
  TERMINEE = 'TERMINEE',
  ANNULEE = 'ANNULEE',
  RENOUVELEE = 'RENOUVELEE'
}

export interface PrescriptionCreateRequest {
  patientId: string;
  diagnostic: string;
  datePrescription: string;
  dureeValiditeJours: number;
  medicaments: MedicamentRequest[];
  notes?: string;
  renouvelable?: boolean;
  nombreRenouvellementsAutorises?: number;
}

export interface MedicamentRequest {
  nomCommercial: string;
  dci: string;
  formePharmaceutique?: string;
  dosage?: string;
  posologie: PosologieRequest;
  instructionsSpeciales?: string;
  substitutable?: boolean;
}

export interface PosologieRequest {
  quantite: number;
  unite: string;
  frequence: string;
  momentPrise?: string;
  dureeTraitementJours?: number;
  isPediatrique?: boolean;
  poidsPatientKg?: number;
  doseParKg?: number;
  surfaceCorporelleM2?: number;
  ajustementRenal?: boolean;
  ajustementHepatique?: boolean;
  justificationAjustement?: string;
}
