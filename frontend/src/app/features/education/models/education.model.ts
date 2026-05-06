export interface Article {
  id: string;
  title: string;
  simplifiedTitle: string;
  category: EducationCategory;
  content: string;
  icon: string;
  tags: string[];
  readTime: number;
}

export interface Video {
  id: string;
  title: string;
  youtubeId?: string;
  duration: string;
  category: EducationCategory;
  thumbnail: string;
  description: string;
}

export interface QuizQuestion {
  id: string;
  question: string;
  options: string[];
  correctIndex: number;
  explanation: string;
  category: EducationCategory;
}

export interface DailyTip {
  icon: string;
  title: string;
  message: string;
  category: 'nutrition' | 'medication' | 'monitoring' | 'lifestyle';
}

export enum EducationCategory {
  KIDNEY_DISEASE = 'kidney_disease',
  MEDICATION = 'medication',
  NUTRITION = 'nutrition',
  MONITORING = 'monitoring',
  EMERGENCY = 'emergency'
}

export interface CategoryInfo {
  key: EducationCategory;
  label: string;
  icon: string;
  color: string;
}
