export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  senderType: SenderType;
  content: string;
  attachments?: Attachment[];
  isRead: boolean;
  readAt?: Date;
  createdAt: Date;

  priority?: MessagePriority;
  replyToId?: string;
  replyTo?: Message;
  reactions?: { [emoji: string]: string[] };
  isDeleted?: boolean;
  editedAt?: Date;
  editHistory?: string[];
}

export enum SenderType {
  DOCTOR = 'DOCTOR',
  PARENT = 'PARENT'
}

export enum MessagePriority {
  NORMAL = 'NORMAL',
  URGENT = 'URGENT'
}

export interface Attachment {
  fileName: string;
  fileUrl: string;
  fileType: string;
  fileSize: number;
}

export interface SendMessageRequest {
  conversationId: string;
  senderId: string;
  senderType: SenderType;
  content: string;
  attachments?: Attachment[];
  priority?: MessagePriority;
  replyToId?: string;
}
