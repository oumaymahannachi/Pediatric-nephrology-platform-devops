import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PatientDossier, MedicalHistoryEntry, FamilyHistoryEntry,
  Allergy, ChatConversation, ChatRequest, ChatResponse,
  SearchRequest, SearchResponse
} from '../models/patient.model';
import { ApiMessage } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly API = `${environment.apiUrl}/patient`;

  constructor(private http: HttpClient) {}

  getDossier(childId: string): Observable<PatientDossier> {
    return this.http.get<PatientDossier>(`${this.API}/dossier/${childId}`);
  }

  updateDossier(childId: string, data: Partial<PatientDossier>): Observable<PatientDossier> {
    return this.http.put<PatientDossier>(`${this.API}/dossier/${childId}`, data);
  }

  getMedicalHistory(childId: string): Observable<MedicalHistoryEntry[]> {
    return this.http.get<MedicalHistoryEntry[]>(`${this.API}/dossier/${childId}/medical-history`);
  }

  addMedicalHistory(childId: string, entry: Partial<MedicalHistoryEntry>): Observable<MedicalHistoryEntry> {
    return this.http.post<MedicalHistoryEntry>(`${this.API}/dossier/${childId}/medical-history`, entry);
  }

  updateMedicalHistory(childId: string, entryId: string, entry: Partial<MedicalHistoryEntry>): Observable<MedicalHistoryEntry> {
    return this.http.put<MedicalHistoryEntry>(`${this.API}/dossier/${childId}/medical-history/${entryId}`, entry);
  }

  deleteMedicalHistory(childId: string, entryId: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.API}/dossier/${childId}/medical-history/${entryId}`);
  }

  getFamilyHistory(childId: string): Observable<FamilyHistoryEntry[]> {
    return this.http.get<FamilyHistoryEntry[]>(`${this.API}/dossier/${childId}/family-history`);
  }

  addFamilyHistory(childId: string, entry: Partial<FamilyHistoryEntry>): Observable<FamilyHistoryEntry> {
    return this.http.post<FamilyHistoryEntry>(`${this.API}/dossier/${childId}/family-history`, entry);
  }

  updateFamilyHistory(childId: string, entryId: string, entry: Partial<FamilyHistoryEntry>): Observable<FamilyHistoryEntry> {
    return this.http.put<FamilyHistoryEntry>(`${this.API}/dossier/${childId}/family-history/${entryId}`, entry);
  }

  deleteFamilyHistory(childId: string, entryId: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.API}/dossier/${childId}/family-history/${entryId}`);
  }

  getAllergies(childId: string): Observable<Allergy[]> {
    return this.http.get<Allergy[]>(`${this.API}/dossier/${childId}/allergies`);
  }

  addAllergy(childId: string, allergy: Partial<Allergy>): Observable<Allergy> {
    return this.http.post<Allergy>(`${this.API}/dossier/${childId}/allergies`, allergy);
  }

  updateAllergy(childId: string, allergyId: string, allergy: Partial<Allergy>): Observable<Allergy> {
    return this.http.put<Allergy>(`${this.API}/dossier/${childId}/allergies/${allergyId}`, allergy);
  }

  deleteAllergy(childId: string, allergyId: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.API}/dossier/${childId}/allergies/${allergyId}`);
  }

  chat(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.API}/ai/chat`, request);
  }

  getChatHistory(): Observable<ChatConversation[]> {
    return this.http.get<ChatConversation[]>(`${this.API}/ai/history`);
  }

  getConversation(conversationId: string): Observable<ChatConversation> {
    return this.http.get<ChatConversation>(`${this.API}/ai/history/${conversationId}`);
  }

  deleteConversation(conversationId: string): Observable<ApiMessage> {
    return this.http.delete<ApiMessage>(`${this.API}/ai/history/${conversationId}`);
  }

  search(request: SearchRequest): Observable<SearchResponse> {
    return this.http.post<SearchResponse>(`${this.API}/ai/search`, request);
  }

  getMedicalNews(): Observable<{ content: string }> {
    return this.http.get<{ content: string }>(`${this.API}/medicines/news`);
  }

  searchMedicine(request: SearchRequest): Observable<SearchResponse> {
    return this.http.post<SearchResponse>(`${this.API}/medicines/search`, request);
  }
}
