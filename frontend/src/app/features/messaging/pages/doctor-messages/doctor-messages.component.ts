import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MessagingService } from '../../services/messaging.service';
import { Conversation } from '../../models/conversation.model';
import { SenderType } from '../../models/message.model';
import { ChatWindowComponent } from '../../components/chat-window/chat-window.component';
import { AuthService } from '../../../../core/services/auth.service';
import { UserService } from '../../../../core/services/user.service';

@Component({
  selector: 'app-doctor-messages',
  standalone: true,
  imports: [CommonModule, FormsModule, ChatWindowComponent],
  templateUrl: './doctor-messages.component.html',
  styleUrls: ['./doctor-messages.component.css']
})
export class DoctorMessagesComponent implements OnInit {
  conversations: Conversation[] = [];
  selectedConversation: Conversation | null = null;
  loading = false;
  error: string | null = null;
  
  currentUserId = '';
  currentUserType = SenderType.DOCTOR;
  
  searchTerm = '';
  filteredConversations: Conversation[] = [];

  constructor(
    private messagingService: MessagingService,
    private authService: AuthService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.id) {
        this.currentUserId = user.id;
        this.loadConversations();
      } else {
        this.error = 'User not logged in';
      }
    });
  }

  loadConversations(): void {
    if (!this.currentUserId) {
      this.error = 'User ID not found';
      return;
    }
    
    this.loading = true;
    this.error = null;
    
    this.messagingService.getDoctorConversations(this.currentUserId).subscribe({
      next: async (response) => {
        this.conversations = response.data;
        
        await this.enrichConversationsWithNames();
        
        this.filteredConversations = [...this.conversations];
        this.loading = false;
        
        if (this.conversations.length > 0 && !this.selectedConversation) {
          this.selectConversation(this.conversations[0]);
        }
      },
      error: (err) => {
        console.error('Error loading conversations:', err);
        this.error = 'Failed to load conversations';
        this.loading = false;
      }
    });
  }

  async enrichConversationsWithNames(): Promise<void> {
    for (const conv of this.conversations) {
      try {
        const children = await firstValueFrom(this.userService.getChildrenByParent(conv.parentId));
        const child = children?.find(c => c.id === conv.childId);
        if (child) {
          conv.childName = `${child.firstName} ${child.lastName}`;
          conv.parentName = conv.childName;
        }
      } catch (error) {
        console.error('Error fetching names:', error);
      }
    }
  }

  selectConversation(conversation: Conversation): void {
    this.selectedConversation = conversation;
  }

  onSearchChange(): void {
    if (!this.searchTerm.trim()) {
      this.filteredConversations = [...this.conversations];
      return;
    }
    
    const term = this.searchTerm.toLowerCase();
    this.filteredConversations = this.conversations.filter(conv => 
      conv.subject.toLowerCase().includes(term) ||
      conv.parentId.toLowerCase().includes(term) ||
      conv.childId.toLowerCase().includes(term)
    );
  }

  getConversationPreview(conversation: Conversation): string {
    return conversation.subject;
  }

  getConversationTime(conversation: Conversation): string {
    if (!conversation.lastMessageTime) return 'No messages';
    const date = new Date(conversation.lastMessageTime);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  }

  isConversationSelected(conversation: Conversation): boolean {
    return this.selectedConversation?.id === conversation.id;
  }

  refreshConversations(): void {
    this.loadConversations();
  }

  getPatientName(conversation: Conversation): string {
    if (conversation.parentName) {
      return conversation.parentName;
    }
    if (conversation.childName) {
      return conversation.childName;
    }
    return 'Patient ' + conversation.parentId.substring(0, 8);
  }
}
