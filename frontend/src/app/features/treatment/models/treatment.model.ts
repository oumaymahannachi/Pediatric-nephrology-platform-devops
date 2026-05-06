export interface Treatment {
  id: string;
  patientId: string;
  medecinId: string;
  diagnostic: string;
  dateDebut: string;
  dateFin: string;
  objectifTraitement: string;
  notes: string;
  statut: StatutTraitement;
  medicaments: Medicament[];
  recommandations: string[];
}

export enum StatutTraitement {
  EN_COURS = 'EN_COURS',
  TERMINE = 'TERMINE',
  SUSPENDU = 'SUSPENDU',
  ANNULE = 'ANNULE'
}

export interface Medicament {
  nomCommercial: string;
  dci: string;
  formePharmaceutique: string;
  dosage: string;
  posologie: Posologie;
  instructionsSpeciales: string;
}

export interface Posologie {
  quantite: number;
  unite: string;
  frequence: string;
  momentPrise: string;
  dureeTraitementJours: number;
}

export interface TreatmentCreateRequest {
  patientId: string;
  diagnostic: string;
  dateDebut: string;
  dateFin: string;
  objectifTraitement: string;
  notes: string;
  medicaments: MedicamentRequest[];
  recommandations: string[];
}

export interface MedicamentRequest {
  nomCommercial: string;
  dci: string;
  formePharmaceutique: string;
  dosage: string;
  posologie: PosologieRequest;
  instructionsSpeciales: string;
}

export interface PosologieRequest {
  quantite: number;
  unite: string;
  frequence: string;
  momentPrise: string;
  dureeTraitementJours: number;
}
