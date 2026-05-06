import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { MessagingService } from '../../services/messaging.service';
import { Message, MessagePriority, SenderType, SendMessageRequest } from '../../models/message.model';
import { Conversation } from '../../models/conversation.model';
import { interval, Subscription, firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-chat-window',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-window.component.html',
  styleUrls: ['./chat-window.component.css']
})
export class ChatWindowComponent implements OnInit, OnDestroy {
  @Input() conversation!: Conversation;
  @Input() currentUserId!: string;
  @Input() currentUserType!: SenderType;

  messages: Message[] = [];
  filteredMessages: Message[] = [];
  newMessageContent = '';
  loading = false;
  sending = false;
  error: string | null = null;

  selectedFiles: File[] = [];
  uploadingFiles = false;

  selectedPriority: MessagePriority = MessagePriority.NORMAL;
  MessagePriority = MessagePriority;
  showPriorityMenu = false;

  replyToMessage: Message | null = null;

  editingMessage: Message | null = null;
  editContent = '';

  searchQuery = '';
  isSearching = false;

  showReactionPickerFor: string | null = null;
  availableReactions = ['👍', '✅', '❓', '❤️', '😊', '⚠️'];

  typingText = '';

  isRecording = false;
  recordingDuration = 0;
  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks: Blob[] = [];
  private recordingTimer: any = null;

  showGallery = false;
  galleryImages: { url: string, name: string }[] = [];
  galleryFiles: { url: string, name: string, size: number }[] = [];
  galleryTab: 'images' | 'files' = 'images';

  pdfPreviewUrl: SafeResourceUrl | null = null;

  constructor(private messagingService: MessagingService, private sanitizer: DomSanitizer) {}

  private refreshSubscription?: Subscription;

  ngOnInit(): void {
    this.loadMessages();
    this.markAsRead();
    this.refreshSubscription = interval(5000).subscribe(() => {
      this.loadMessages(true);
    });
  }

  ngOnDestroy(): void {
    this.refreshSubscription?.unsubscribe();
    if (this.isRecording) this.cancelRecording();
  }

  loadMessages(silent = false): void {
    if (!silent) this.loading = true;
    this.error = null;

    this.messagingService.getConversationMessages(this.conversation.id).subscribe({
      next: (response: any) => {
        this.messages = response.data;
        this.applySearch();
        this.loading = false;
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: (err: any) => {
        this.error = 'Error loading messages';
        this.loading = false;
      }
    });
  }

  applySearch(): void {
    if (!this.searchQuery.trim()) {
      this.filteredMessages = [...this.messages];
    } else {
      const q = this.searchQuery.toLowerCase();
      this.filteredMessages = this.messages.filter(m =>
        m.content?.toLowerCase().includes(q)
      );
    }
  }

  onSearchChange(): void {
    this.applySearch();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.applySearch();
  }

  async sendMessage(): Promise<void> {
    if (this.editingMessage) {
      this.saveEdit();
      return;
    }

    if (!this.newMessageContent.trim() && this.selectedFiles.length === 0) return;

    this.sending = true;
    this.error = null;

    try {
      const attachments = [];
      if (this.selectedFiles.length > 0) {
        this.uploadingFiles = true;
        for (const file of this.selectedFiles) {
          const formData = new FormData();
          formData.append('file', file);
          const uploadResponse = await firstValueFrom(this.messagingService.uploadFile(formData));
          if (uploadResponse?.data) attachments.push(uploadResponse.data);
        }
        this.uploadingFiles = false;
      }

      const request: any = {
        conversationId: this.conversation.id,
        senderId: this.currentUserId,
        senderType: this.currentUserType,
        content: this.newMessageContent.trim() || '📎 Attachment',
        attachments,
        priority: this.selectedPriority,
        replyToId: this.replyToMessage?.id || null
      };

      this.messagingService.sendMessage(request).subscribe({
        next: (response: any) => {
          this.messages.push(response.data);
          this.filteredMessages = [...this.messages];
          this.newMessageContent = '';
          this.selectedFiles = [];
          this.replyToMessage = null;
          this.selectedPriority = MessagePriority.NORMAL;
          this.sending = false;
          setTimeout(() => this.scrollToBottom(), 100);
        },
        error: () => {
          this.error = 'Error sending message';
          this.sending = false;
          this.uploadingFiles = false;
        }
      });
    } catch {
      this.error = 'Error uploading files';
      this.sending = false;
      this.uploadingFiles = false;
    }
  }

  setReply(message: Message): void {
    this.replyToMessage = message;
    this.editingMessage = null;
  }

  cancelReply(): void {
    this.replyToMessage = null;
  }

  startEdit(message: Message): void {
    this.editingMessage = message;
    this.editContent = message.content;
    this.newMessageContent = message.content;
    this.replyToMessage = null;
  }

  cancelEdit(): void {
    this.editingMessage = null;
    this.editContent = '';
    this.newMessageContent = '';
  }

  saveEdit(): void {
    if (!this.editingMessage || !this.newMessageContent.trim()) return;
    this.messagingService.editMessage(this.editingMessage.id, this.currentUserId, this.newMessageContent.trim()).subscribe({
      next: (response: any) => {
        const idx = this.messages.findIndex(m => m.id === this.editingMessage!.id);
        if (idx !== -1) this.messages[idx] = response.data;
        this.filteredMessages = [...this.messages];
        this.editingMessage = null;
        this.newMessageContent = '';
      },
      error: () => { this.error = 'Error editing message'; }
    });
  }

  deleteMessage(message: Message): void {
    if (!confirm('Delete this message?')) return;
    this.messagingService.deleteMessage(message.id, this.currentUserId).subscribe({
      next: (response: any) => {
        const idx = this.messages.findIndex(m => m.id === message.id);
        if (idx !== -1) this.messages[idx] = response.data;
        this.filteredMessages = [...this.messages];
      },
      error: () => { this.error = 'Error deleting message'; }
    });
  }

  toggleReactionPicker(messageId: string): void {
    this.showReactionPickerFor = this.showReactionPickerFor === messageId ? null : messageId;
  }

  addReaction(message: Message, emoji: string): void {
    this.showReactionPickerFor = null;
    this.messagingService.addReaction(message.id, emoji, this.currentUserId).subscribe({
      next: (response: any) => {
        const idx = this.messages.findIndex(m => m.id === message.id);
        if (idx !== -1) this.messages[idx] = response.data;
        this.filteredMessages = [...this.messages];
      }
    });
  }

  getReactionEntries(reactions: { [emoji: string]: string[] } | undefined): { emoji: string, count: number, hasReacted: boolean }[] {
    if (!reactions) return [];
    return Object.entries(reactions).map(([emoji, users]) => ({
      emoji,
      count: users.length,
      hasReacted: users.includes(this.currentUserId)
    }));
  }

  async startRecording(): Promise<void> {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.audioChunks = [];
      this.mediaRecorder = new MediaRecorder(stream);

      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) this.audioChunks.push(event.data);
      };

      this.mediaRecorder.onstop = async () => {
        const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
        const audioFile = new File([audioBlob], `voice-${Date.now()}.webm`, { type: 'audio/webm' });
        stream.getTracks().forEach(t => t.stop());
        await this.sendVoiceMessage(audioFile);
      };

      this.mediaRecorder.start();
      this.isRecording = true;
      this.recordingDuration = 0;
      this.recordingTimer = setInterval(() => this.recordingDuration++, 1000);
    } catch (err) {
      alert('Microphone access denied. Please allow microphone access.');
    }
  }

  stopRecording(): void {
    if (this.mediaRecorder && this.isRecording) {
      this.mediaRecorder.stop();
      this.isRecording = false;
      clearInterval(this.recordingTimer);
      this.recordingDuration = 0;
    }
  }

  cancelRecording(): void {
    if (this.mediaRecorder && this.isRecording) {
      this.mediaRecorder.ondataavailable = null;
      this.mediaRecorder.onstop = null;
      this.mediaRecorder.stop();
      this.mediaRecorder.stream?.getTracks().forEach(t => t.stop());
    }
    this.isRecording = false;
    this.audioChunks = [];
    clearInterval(this.recordingTimer);
    this.recordingDuration = 0;
  }

  async sendVoiceMessage(audioFile: File): Promise<void> {
    this.sending = true;
    console.log('Sending voice message, file size:', audioFile.size);
    try {
      const formData = new FormData();
      formData.append('file', audioFile);
      const uploadResponse = await firstValueFrom(this.messagingService.uploadFile(formData));
      console.log('Upload response:', uploadResponse);

      if (uploadResponse?.data) {
        const request: any = {
          conversationId: this.conversation.id,
          senderId: this.currentUserId,
          senderType: this.currentUserType,
          content: '🎤 Voice message',
          attachments: [uploadResponse.data],
          priority: MessagePriority.NORMAL
        };

        this.messagingService.sendMessage(request).subscribe({
          next: (response: any) => {
            this.messages.push(response.data);
            this.filteredMessages = [...this.messages];
            this.sending = false;
            setTimeout(() => this.scrollToBottom(), 100);
          },
          error: () => { this.error = 'Error sending voice message'; this.sending = false; }
        });
      }
    } catch {
      this.error = 'Error uploading voice message';
      this.sending = false;
    }
  }

  formatDuration(seconds: number): string {
    const m = Math.floor(seconds / 60).toString().padStart(2, '0');
    const s = (seconds % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  }

  isVoiceMessage(message: Message): boolean {
    return message.attachments?.some(a => a.fileType?.startsWith('audio/')) ?? false;
  }

  getVoiceUrl(message: Message): string {
    const url = message.attachments?.find(a => a.fileType?.startsWith('audio/'))?.fileUrl ?? '';
    return this.resolveUrl(url);
  }

  private async compressImage(file: File): Promise<File> {
    if (!file.type.startsWith('image/')) return file;
    if (file.size < 200 * 1024) return file;

    return new Promise((resolve) => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d')!;
      const img = new Image();
      img.onload = () => {
        const maxW = 1200, maxH = 1200;
        let { width, height } = img;
        if (width > maxW) { height = height * maxW / width; width = maxW; }
        if (height > maxH) { width = width * maxH / height; height = maxH; }
        canvas.width = width;
        canvas.height = height;
        ctx.drawImage(img, 0, 0, width, height);
        canvas.toBlob(blob => {
          if (blob) resolve(new File([blob], file.name, { type: 'image/jpeg' }));
          else resolve(file);
        }, 'image/jpeg', 0.8);
      };
      img.src = URL.createObjectURL(file);
    });
  }

  openGallery(): void {
    this.galleryImages = [];
    this.galleryFiles = [];
    this.messages.forEach(m => {
      m.attachments?.forEach(a => {
        const url = this.resolveUrl(a.fileUrl);
        if (a.fileType?.startsWith('image/')) {
          this.galleryImages.push({ url, name: a.fileName });
        } else if (!a.fileType?.startsWith('audio/')) {
          this.galleryFiles.push({ url, name: a.fileName, size: a.fileSize });
        }
      });
    });
    this.galleryTab = this.galleryImages.length > 0 ? 'images' : 'files';
    this.showGallery = true;
  }

  closeGallery(): void { this.showGallery = false; }

  openPdfPreview(url: string): void {
    this.pdfPreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.resolveUrl(url));
  }

  closePdfPreview(): void { this.pdfPreviewUrl = null; }

  private resolveUrl(url: string): string {
    if (url && url.startsWith('/api/')) return `http://localhost:9090${url}`;
    return url;
  }

  startAudioCall(): void {
    const roomName = `pedialink-audio-${this.conversation.id}`;
    window.open(`https://meet.jit.si/${roomName}#config.startWithVideoMuted=true`, '_blank');
    const request: any = {
      conversationId: this.conversation.id,
      senderId: this.currentUserId,
      senderType: this.currentUserType,
      content: `📞 Audio call started. Join: https://meet.jit.si/${roomName}`,
      priority: MessagePriority.URGENT
    };
    this.messagingService.sendMessage(request).subscribe({
      next: (response: any) => {
        this.messages.push(response.data);
        this.filteredMessages = [...this.messages];
        setTimeout(() => this.scrollToBottom(), 100);
      }
    });
  }

  startVideoCall(): void {
    const roomName = `pedialink-video-${this.conversation.id}`;
    window.open(`https://meet.jit.si/${roomName}`, '_blank');
    const request: any = {
      conversationId: this.conversation.id,
      senderId: this.currentUserId,
      senderType: this.currentUserType,
      content: `📹 Video call started. Join: https://meet.jit.si/${roomName}`,
      priority: MessagePriority.URGENT
    };
    this.messagingService.sendMessage(request).subscribe({
      next: (response: any) => {
        this.messages.push(response.data);
        this.filteredMessages = [...this.messages];
        setTimeout(() => this.scrollToBottom(), 100);
      }
    });
  }

  markAsRead(): void {
    const isDoctor = this.currentUserType === SenderType.DOCTOR;
    this.messagingService.markConversationAsRead(this.conversation.id, isDoctor).subscribe();
    this.messagingService.markMessagesAsRead(this.conversation.id, this.currentUserId).subscribe();
  }

  isMyMessage(message: Message): boolean {
    return message.senderId === this.currentUserId;
  }

  isUrgent(message: Message): boolean {
    return message.priority === MessagePriority.URGENT;
  }

  private scrollToBottom(): void {
    const container = document.querySelector('.messages-container');
    if (container) container.scrollTop = container.scrollHeight;
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  async onFileSelected(event: any): Promise<void> {
    const files = event.target.files;
    if (!files) return;
    for (let i = 0; i < files.length; i++) {
      let file = files[i];
      if (file.size > 10 * 1024 * 1024) { alert(`${file.name} is too large (max 10MB)`); continue; }
      const allowed = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'];
      if (!allowed.includes(file.type)) { alert(`${file.name} type not allowed`); continue; }
      if (file.type.startsWith('image/')) {
        file = await this.compressImage(file);
      }
      this.selectedFiles.push(file);
    }
  }

  removeFile(index: number): void { this.selectedFiles.splice(index, 1); }

  getFileIcon(fileType: string): string {
    if (fileType.startsWith('image/')) return 'fa-image';
    if (fileType === 'application/pdf') return 'fa-file-pdf';
    return 'fa-file';
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  openAttachment(attachment: any): void {
    window.open(this.resolveUrl(attachment.fileUrl), '_blank');
  }
}
