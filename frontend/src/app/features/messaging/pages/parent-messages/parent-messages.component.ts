import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MessagingService } from '../../services/messaging.service';
import { Conversation, CreateConversationRequest, ContextType } from '../../models/conversation.model';
import { SenderType } from '../../models/message.model';
import { ChatWindowComponent } from '../../components/chat-window/chat-window.component';
import { NewConversationModalComponent } from '../../components/new-conversation-modal/new-conversation-modal.component';
import { AuthService } from '../../../../core/services/auth.service';
import { UserService } from '../../../../core/services/user.service';

@Component({
  selector: 'app-parent-messages',
  standalone: true,
  imports: [CommonModule, FormsModule, ChatWindowComponent, NewConversationModalComponent],
  templateUrl: './parent-messages.component.html',
  styleUrls: ['./parent-messages.component.css']
})
export class ParentMessagesComponent implements OnInit {
  conversations: Conversation[] = [];
  selectedConversation: Conversation | null = null;
  loading = false;
  error: string | null = null;
  
  currentUserId = '';
  currentUserType = SenderType.PARENT;
  
  // Search
  searchTerm = '';
  filteredConversations: Conversation[] = [];
  
  // New conversation modal
  showNewConversationModal = false;
  creatingConversation = false;

  constructor(
    private messagingService: MessagingService,
    private authService: AuthService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // Get current user from AuthService
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
    
    this.messagingService.getParentConversations(this.currentUserId).subscribe({
      next: async (response: any) => {
        this.conversations = response.data;
        
        // Enrich conversations with doctor names
        await this.enrichConversationsWithNames();
        
        this.filteredConversations = [...this.conversations];
        this.loading = false;
        
        // Auto-select first conversation if exists
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
    try {
      // Get all doctors once
      const doctors = await firstValueFrom(this.userService.getAllDoctors());
      
      // Enrich each conversation with doctor name
      for (const conv of this.conversations) {
        const doctor = doctors?.find(d => d.id === conv.doctorId);
        if (doctor) {
          conv.doctorName = doctor.fullName;
        }
      }
    } catch (error) {
      console.error('Error fetching doctor names:', error);
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
      conv.doctorId.toLowerCase().includes(term)
    );
  }

  getDoctorName(conversation: Conversation): string {
    // If we have the doctor name, use it
    if (conversation.doctorName) {
      return conversation.doctorName;
    }
    // Otherwise, show a shortened ID
    return 'Dr. ' + conversation.doctorId.substring(0, 8);
  }

  getPatientName(conversation: Conversation): string {
    // If we have the parent name, use it
    if (conversation.parentName) {
      return conversation.parentName;
    }
    // Otherwise, show a shortened ID
    return 'Patient ' + conversation.parentId.substring(0, 8);
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

  openNewConversationModal(): void {
    this.showNewConversationModal = true;
  }
  
  closeNewConversationModal(): void {
    this.showNewConversationModal = false;
  }
  
  onDoctorSelected(data: any): void {
    this.creatingConversation = true;
    this.error = null;
    
    const request: CreateConversationRequest = {
      doctorId: data.doctor.id,
      parentId: this.currentUserId,
      childId: data.child.id,
      subject: data.subject,
      contextType: ContextType.GENERAL
    };
    
    this.messagingService.createConversation(request).subscribe({
      next: (response) => {
        this.creatingConversation = false;
        this.showNewConversationModal = false;
        
        // Add new conversation to list
        const newConversation = response.data;
        this.conversations.unshift(newConversation);
        this.filteredConversations = [...this.conversations];
        
        // Select the new conversation
        this.selectConversation(newConversation);
        
        // Show success message
        alert('Conversation created! You can now send messages.');
      },
      error: (err) => {
        console.error('Error creating conversation:', err);
        this.error = 'Failed to create conversation';
        this.creatingConversation = false;
      }
    });
  }
}
