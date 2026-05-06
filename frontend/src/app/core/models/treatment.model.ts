export interface GrowthMeasurement {
  id?: string;
  childId: string;
  date: string;
  weight: number;
  height: number;
  bmi?: number;
  headCircumference?: number;
  notes?: string;
  recordedBy?: string;
  createdAt?: string;
}

export interface DietaryRestriction {
  id?: string;
  childId: string;
  type: string;
  allergen: string;
  severity: string;
  description?: string;
  notes?: string;
  createdAt?: string;
}

export interface NutritionalPlan {
  id?: string;
  childId: string;
  doctorId?: string;
  title: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  status?: string;
  goals?: string;
  restrictions?: string;
  meals?: Meal[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Meal {
  name: string;
  time: string;
  description?: string;
  calories?: number;
  notes?: string;
}
