export interface VaccinationRecord {
  vaccineName: string;
  dateAdministered: string;
  nextDueDate?: string;
  notes?: string;
}

export interface PatientDossier {
  id?: string;
  childId: string;
  parentId?: string;
  bloodGroup?: string;
  renalPathologies?: string[];
  vaccinations?: VaccinationRecord[];
  chronicConditions?: string;
  currentMedications?: string;
  emergencyContact?: string;
  personalNotes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MedicalHistoryEntry {
  id?: string;
  childId?: string;
  date: string;
  diagnosis: string;
  doctorName?: string;
  hospitalName?: string;
  description?: string;
  treatment?: string;
  outcome?: string;
  notes?: string;
  createdAt?: string;
}

export interface FamilyHistoryEntry {
  id?: string;
  childId?: string;
  relation: string;
  condition: string;
  ageOfOnset?: string;
  description?: string;
  notes?: string;
  createdAt?: string;
}

export interface Allergy {
  id?: string;
  childId?: string;
  allergen: string;
  allergyType: string;
  severity: string;
  symptoms?: string;
  discoveredDate?: string;
  treatment?: string;
  notes?: string;
  createdAt?: string;
}

export interface ChatMessage {
  role: 'USER' | 'ASSISTANT';
  content: string;
  timestamp?: string;
}

export interface ChatConversation {
  id?: string;
  parentId?: string;
  title?: string;
  conversationType?: string;
  messages?: ChatMessage[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ChatRequest {
  message: string;
  conversationId?: string;
  childId?: string;
  type?: string;
}

export interface ChatResponse {
  conversationId: string;
  reply: string;
  role: string;
}

export interface SearchRequest {
  query: string;
  childId?: string;
}

export interface SearchResponse {
  query: string;
  answer: string;
  disclaimer: string;
}
