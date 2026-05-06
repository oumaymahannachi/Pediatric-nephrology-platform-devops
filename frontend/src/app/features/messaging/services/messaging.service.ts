import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Conversation, CreateConversationRequest } from '../models/conversation.model';
import { Message, SendMessageRequest } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class MessagingService {
  private conversationsUrl = `${environment.apiUrl}/conversations`;
  private messagesUrl = `${environment.apiUrl}/messages`;

  constructor(private http: HttpClient) {}

  createConversation(request: CreateConversationRequest): Observable<any> {
    return this.http.post(`${this.conversationsUrl}`, request);
  }

  getDoctorConversations(doctorId: string): Observable<any> {
    return this.http.get(`${this.conversationsUrl}/doctor/${doctorId}`);
  }

  getParentConversations(parentId: string): Observable<any> {
    return this.http.get(`${this.conversationsUrl}/parent/${parentId}`);
  }

  getConversation(id: string): Observable<any> {
    return this.http.get(`${this.conversationsUrl}/${id}`);
  }

  markConversationAsRead(id: string, isDoctor: boolean): Observable<any> {
    return this.http.put(`${this.conversationsUrl}/${id}/mark-read?isDoctor=${isDoctor}`, {});
  }

  sendMessage(request: SendMessageRequest): Observable<any> {
    return this.http.post(`${this.messagesUrl}`, request);
  }

  getConversationMessages(conversationId: string): Observable<any> {
    return this.http.get(`${this.messagesUrl}/conversation/${conversationId}`);
  }

  getConversationMessagesPaginated(conversationId: string, page: number = 0, size: number = 50): Observable<any> {
    return this.http.get(`${this.messagesUrl}/conversation/${conversationId}/paginated?page=${page}&size=${size}`);
  }

  markMessagesAsRead(conversationId: string, userId: string): Observable<any> {
    return this.http.put(`${this.messagesUrl}/conversation/${conversationId}/mark-read?userId=${userId}`, {});
  }

  getUnreadCount(conversationId: string): Observable<any> {
    return this.http.get(`${this.messagesUrl}/conversation/${conversationId}/unread-count`);
  }

  searchMessages(conversationId: string, query: string): Observable<any> {
    return this.http.get(`${this.messagesUrl}/conversation/${conversationId}/search?query=${encodeURIComponent(query)}`);
  }

  addReaction(messageId: string, emoji: string, userId: string): Observable<any> {
    return this.http.post(`${this.messagesUrl}/${messageId}/reactions`, { emoji, userId });
  }

  editMessage(messageId: string, senderId: string, content: string): Observable<any> {
    return this.http.put(`${this.messagesUrl}/${messageId}`, { senderId, content });
  }

  deleteMessage(messageId: string, senderId: string): Observable<any> {
    return this.http.delete(`${this.messagesUrl}/${messageId}?senderId=${senderId}`);
  }

  uploadFile(formData: FormData): Observable<any> {
    return this.http.post(`${environment.apiUrl}/files/upload`, formData);
  }
}
