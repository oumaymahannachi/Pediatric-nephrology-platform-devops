export interface Dialyse {
  id?: string;
  patientId: string;
  dateSeance: string;
  heureDebut: string;
  heureFin: string;
  dureeSeance?: number;
  typeDialyse: TypeDialyse;
  accesVasculaire: AccesVasculaire;
  parametresTechniques: ParametresTechniques;
  bilanSeance: BilanSeance;
  complications?: Complication[];
  observations?: string;
  medecinId: string;
  medecinNom?: string;
  infirmierId?: string;
  infirmierNom?: string;
}

export enum TypeDialyse {
  HEMODIALYSE = 'HEMODIALYSE',
  DIALYSE_PERITONEALE = 'DIALYSE_PERITONEALE',
  HEMOFILTRATION = 'HEMOFILTRATION'
}

export enum AccesVasculaire {
  FISTULE_ARTERIO_VEINEUSE = 'FISTULE_ARTERIO_VEINEUSE',
  CATHETER_CENTRAL = 'CATHETER_CENTRAL',
  CATHETER_PERITONEALE = 'CATHETER_PERITONEALE',
  PROTHESE_VASCULAIRE = 'PROTHESE_VASCULAIRE'
}

export interface ParametresTechniques {
  debitSanguin?: number;
  debitDialysat?: number;
  ultrafiltration?: number;
  anticoagulation?: string;
  doseAnticoagulant?: number;
  typeDialyseur?: string;
  surfaceDialyseur?: number;
}

export interface BilanSeance {
  poidsAvant: number;
  poidsApres: number;
  pertePoids?: number;
  tensionAvant: string;
  tensionApres: string;
  frequenceCardiaqueAvant?: number;
  frequenceCardiaqueApres?: number;
  temperatureAvant?: number;
  temperatureApres?: number;
}

export interface Complication {
  type: string;
  severite: 'LEGERE' | 'MODEREE' | 'SEVERE';
  description: string;
  traitement?: string;
}

export interface DialyseCreateRequest {
  patientId: string;
  dateSeance: string;
  heureDebut: string;
  heureFin: string;
  typeDialyse: TypeDialyse;
  accesVasculaire: AccesVasculaire;
  parametresTechniques: ParametresTechniques;
  bilanSeance: BilanSeance;
  complications?: Complication[];
  observations?: string;
}
