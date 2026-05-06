export type DialysisType = 'HEMODIALYSIS' | 'PERITONEAL' | 'HEMOFILTRATION' | 'HEMODIAFILTRATION';
export type SessionStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'MISSED';

export interface DialysisPrescription {
  id?: string;
  patientId: string;
  patientName?: string;
  medecinId: string;
  type: DialysisType;
  frequencyPerWeek: number;
  sessionDurationMinutes: number;
  bloodFlowRate?: number;
  dialysateFlowRate?: number;
  anticoagulation?: string;
  vascularAccess?: string;
  notes?: string;
  startDate: string;
  endDate?: string;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface DialysisSession {
  id?: string;
  prescriptionId: string;
  patientId: string;
  patientName?: string;
  medecinId: string;
  scheduledDate: string;
  startTime?: string;
  endTime?: string;
  status?: SessionStatus;
  
  preWeight?: number;
  preBloodPressureSystolic?: number;
  preBloodPressureDiastolic?: number;
  prePulse?: number;
  preTemperature?: number;
  
  postWeight?: number;
  postBloodPressureSystolic?: number;
  postBloodPressureDiastolic?: number;
  postPulse?: number;
  postTemperature?: number;
  
  ultrafiltrationVolume?: number;
  complications?: string;
  notes?: string;
  
  createdAt?: string;
  updatedAt?: string;
}
