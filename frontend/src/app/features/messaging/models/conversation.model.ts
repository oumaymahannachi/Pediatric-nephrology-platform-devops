export interface Conversation {
  id: string;
  doctorId: string;
  parentId: string;
  childId: string;
  subject: string;
  
  contextType?: ContextType;
  contextId?: string;
  
  lastMessageContent?: string;
  lastMessageTime?: Date;
  lastMessageSenderId?: string;
  
  doctorUnreadCount: number;
  parentUnreadCount: number;
  
  status: ConversationStatus;
  
  createdAt: Date;
  updatedAt: Date;
  
  doctorName?: string;
  parentName?: string;
  childName?: string;
}

export enum ContextType {
  PRESCRIPTION = 'PRESCRIPTION',
  TREATMENT = 'TREATMENT',
  LAB_RESULT = 'LAB_RESULT',
  GENERAL = 'GENERAL'
}

export enum ConversationStatus {
  ACTIVE = 'ACTIVE',
  ARCHIVED = 'ARCHIVED',
  CLOSED = 'CLOSED'
}

export interface CreateConversationRequest {
  doctorId: string;
  parentId: string;
  childId: string;
  subject: string;
  contextType?: ContextType;
  contextId?: string;
}
