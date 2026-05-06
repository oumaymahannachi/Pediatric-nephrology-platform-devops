export type AppointmentStatus = 'PENDING' | 'ACCEPTED' | 'REFUSED' | 'RESCHEDULED' | 'COMPLETED' | 'CANCELLED';

export interface Appointment {
  id?: string;
  childId: string;
  doctorId: string;
  parentId?: string;
  dateTime: string;
  proposedDateTime?: string;
  status?: AppointmentStatus;
  reason?: string;
  parentNotes?: string;
  doctorNotes?: string;
  createdAt?: string;
  updatedAt?: string;
}
