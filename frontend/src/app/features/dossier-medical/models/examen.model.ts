export interface Examen {
  id?: string;
  patientId: string;
  dateExamen: string;
  typeExamen: TypeExamen;
  nomExamen: string;
  laboratoire?: string;
  prescripteur?: string;
  prescripteurNom?: string;
  resultats: ResultatExamen[];
  interpretation?: string;
  conclusion?: string;
  anormal: boolean;
  urgent: boolean;
  fichierUrl?: string;
  commentaires?: string;
}

export enum TypeExamen {
  BIOLOGIE = 'BIOLOGIE',
  IMAGERIE = 'IMAGERIE',
  BIOPSIE = 'BIOPSIE',
  CARDIOLOGIE = 'CARDIOLOGIE',
  AUTRE = 'AUTRE'
}

export interface ResultatExamen {
  parametre: string;
  valeur: string;
  unite?: string;
  valeurReference?: string;
  anormal?: boolean;
}

export interface ExamenCreateRequest {
  patientId: string;
  dateExamen: string;
  typeExamen: TypeExamen;
  nomExamen: string;
  laboratoire?: string;
  resultats: ResultatExamen[];
  interpretation?: string;
  conclusion?: string;
  anormal: boolean;
  urgent: boolean;
  commentaires?: string;
}

// Paramètres biologiques courants en néphrologie pédiatrique
export const PARAMETRES_BIOLOGIQUES = [
  { nom: 'Créatinine', unite: 'mg/dL', reference: '0.3-0.7' },
  { nom: 'Urée', unite: 'mg/dL', reference: '10-40' },
  { nom: 'DFG (Débit de Filtration Glomérulaire)', unite: 'mL/min/1.73m²', reference: '>90' },
  { nom: 'Sodium', unite: 'mmol/L', reference: '135-145' },
  { nom: 'Potassium', unite: 'mmol/L', reference: '3.5-5.0' },
  { nom: 'Calcium', unite: 'mg/dL', reference: '8.5-10.5' },
  { nom: 'Phosphore', unite: 'mg/dL', reference: '4.0-7.0' },
  { nom: 'Hémoglobine', unite: 'g/dL', reference: '11-16' },
  { nom: 'Albumine', unite: 'g/dL', reference: '3.5-5.0' },
  { nom: 'Protéinurie', unite: 'mg/24h', reference: '<150' }
];
