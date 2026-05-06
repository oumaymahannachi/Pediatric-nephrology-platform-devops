export interface BloodTest {
  id?: string;
  patientId: string;
  patientName?: string;
  medecinId: string;
  medecinName?: string;
  testDate: string;
  testType: string;
  results?: Map<string, TestValue>;
  laboratoryName?: string;
  notes?: string;
  interpretation?: string;
  abnormal?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface TestValue {
  value: number;
  unit: string;
  referenceRange: string;
  isAbnormal?: boolean;
}

export interface LabResult {
  id?: string;
  patientId: string;
  patientName?: string;
  medecinId: string;
  medecinName?: string;
  testDate: string;
  testType: string;
  testName: string;
  findings?: string;
  result?: string;
  details?: string;
  laboratoryName?: string;
  specimenType?: string;
  notes?: string;
  abnormal?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface MedicalImaging {
  id?: string;
  patientId: string;
  patientName?: string;
  medecinId: string;
  medecinName?: string;
  imagingDate: string;
  imagingType: string;
  bodyPart: string;
  indication?: string;
  findings?: string;
  impression?: string;
  recommendation?: string;
  radiologistName?: string;
  performedBy?: string;
  facilityName?: string;
  imageUrls?: string[];
  documentUrls?: string[];
  urgencyLevel?: string;
  followUpRequired?: boolean;
  followUpDate?: string;
  status?: string;
  notes?: string;
  abnormal?: boolean;
  createdAt?: string;
  updatedAt?: string;
}
