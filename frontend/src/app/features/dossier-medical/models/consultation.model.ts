export interface Consultation {
  id?: string;
  patientId: string;
  medecinId: string;
  dateRendezVous: string;
  dateProposee?: string;
  motifConsultation?: string;
  observationsCliniques?: string;
  diagnostic?: string;
  recommandations?: string;
  compteRendu?: string;
  statut: 'EN_ATTENTE' | 'ACCEPTEE' | 'REFUSEE' | 'TERMINEE';
  raisonRefus?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ConsultationCreateRequest {
  patientId: string;
  medecinId: string;
  dateRendezVous: string;
  motifConsultation: string;
}

export interface ConsultationResponseRequest {
  dateProposee?: string;
  raisonRefus?: string;
}
