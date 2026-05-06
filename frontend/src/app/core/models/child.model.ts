export interface Child {
  id?: string;
  fullName: string;
  dateOfBirth: string;
  gender: string;
  parentId?: string;
  doctorIds?: string[];
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DoctorInfo {
  id: string;
  fullName: string;
  specialization: string;
  clinicName: string;
}
