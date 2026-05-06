export enum TestType {
  BLOOD = 'BLOOD',
  URINE = 'URINE',
  BIOPSY = 'BIOPSY',
  OTHER = 'OTHER'
}

export enum ResultStatus {
  PENDING = 'PENDING',
  VALIDATED = 'VALIDATED',
  REVIEWED = 'REVIEWED'
}

export enum CKDStage {
  STAGE_1 = 'STAGE_1',
  STAGE_2 = 'STAGE_2',
  STAGE_3A = 'STAGE_3A',
  STAGE_3B = 'STAGE_3B',
  STAGE_4 = 'STAGE_4',
  STAGE_5 = 'STAGE_5',
  UNKNOWN = 'UNKNOWN'
}

export interface LabResult {
  id: string;
  patientId: string;
  doctorId: string;
  testDate: string;
  testType: TestType;
  
  // Blood tests
  creatinine?: number;
  urea?: number;
  sodium?: number;
  potassium?: number;
  calcium?: number;
  phosphorus?: number;
  hemoglobin?: number;
  albumin?: number;
  bicarbonate?: number;
  
  // Urine tests
  urineProtein?: number;
  urineCreatinine?: number;
  urineAspect?: string;
  urineAlbumin?: number;
  
  // Calculated values
  eGFR?: number;
  ckdStage?: CKDStage;
  proteinCreatinineRatio?: number;
  
  // Metadata
  doctorNotes?: string;
  isAbnormal: boolean;
  alerts: string[];
  laboratoryName?: string;
  status: ResultStatus;
  
  createdAt: string;
  updatedAt?: string;
}

export interface CreateLabResultRequest {
  patientId: string;
  testDate: string;
  testType: TestType;
  
  creatinine?: number;
  urea?: number;
  sodium?: number;
  potassium?: number;
  calcium?: number;
  phosphorus?: number;
  hemoglobin?: number;
  albumin?: number;
  bicarbonate?: number;
  
  urineProtein?: number;
  urineCreatinine?: number;
  urineAspect?: string;
  urineAlbumin?: number;
  
  doctorNotes?: string;
  laboratoryName?: string;
}
