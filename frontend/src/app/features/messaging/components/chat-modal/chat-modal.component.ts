import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessagingService } from '../../services/messaging.service';
import { Conversation, CreateConversationRequest, ContextType } from '../../models/conversation.model';
import { SenderType } from '../../models/message.model';
import { ChatWindowComponent } from '../chat-window/chat-window.component';

@Component({
  selector: 'app-chat-modal',
  standalone: true,
  imports: [CommonModule, ChatWindowComponent],
  templateUrl: './chat-modal.component.html',
  styleUrls: ['./chat-modal.component.css']
})
export class ChatModalComponent implements OnInit {
  @Input() show = false;
  @Input() doctorId!: string;
  @Input() parentId!: string;
  @Input() childId!: string;
  @Input() currentUserId!: string;
  @Input() currentUserType!: SenderType;
  @Input() subject = 'Medical Consultation';
  @Input() contextType?: ContextType;
  @Input() contextId?: string;
  
  @Output() close = new EventEmitter<void>();

  conversation: Conversation | null = null;
  loading = false;
  error: string | null = null;

  constructor(private messagingService: MessagingService) {}

  ngOnInit(): void {
    if (this.show) {
      this.loadOrCreateConversation();
    }
  }

  loadOrCreateConversation(): void {
    this.loading = true;
    this.error = null;

    const request: CreateConversationRequest = {
      doctorId: this.doctorId,
      parentId: this.parentId,
      childId: this.childId,
      subject: this.subject,
      contextType: this.contextType,
      contextId: this.contextId
    };

    this.messagingService.createConversation(request).subscribe({
      next: (response: any) => {
        this.conversation = response.data;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error creating conversation:', err);
        this.error = 'Error loading chat. Please try again.';
        this.loading = false;
      }
    });
  }

  closeModal(): void {
    this.close.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.closeModal();
    }
  }
}
