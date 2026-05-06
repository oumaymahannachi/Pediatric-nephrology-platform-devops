import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { UserService, Doctor, Child } from '../../../../core/services/user.service';

@Component({
  selector: 'app-new-conversation-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './new-conversation-modal.component.html',
  styleUrls: ['./new-conversation-modal.component.css']
})
export class NewConversationModalComponent implements OnInit {
  @Output() close = new EventEmitter<void>();
  @Output() doctorSelected = new EventEmitter<any>();

  doctors: Doctor[] = [];
  children: Child[] = [];
  
  selectedDoctorId = '';
  selectedChildId = '';
  subject = '';
  
  loading = false;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadDoctors();
    this.loadChildren();
  }

  loadDoctors(): void {
    this.loading = true;
    this.userService.getAllDoctors().subscribe({
      next: (doctors) => {
        this.doctors = doctors;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading doctors:', err);
        this.error = 'Failed to load doctors';
        this.loading = false;
      }
    });
  }

  loadChildren(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.id) {
        this.userService.getChildrenByParent(user.id).subscribe({
          next: (children) => {
            this.children = children;
            // Auto-select first child
            if (this.children.length > 0) {
              this.selectedChildId = this.children[0].id;
            } else {
              // No children found
              this.error = 'No children found. Please add a child first from your dashboard.';
            }
          },
          error: (err) => {
            console.error('Error loading children:', err);
            if (err.status === 404 || err.status === 0) {
              this.error = 'No children found. Please add a child first from your dashboard.';
            } else {
              this.error = 'Failed to load children. Please try again.';
            }
          }
        });
      }
    });
  }

  onClose(): void {
    this.close.emit();
  }

  onSubmit(): void {
    if (!this.selectedDoctorId || !this.selectedChildId || !this.subject.trim()) {
      this.error = 'Please fill in all fields';
      return;
    }

    const selectedDoctor = this.doctors.find(d => d.id === this.selectedDoctorId);
    const selectedChild = this.children.find(c => c.id === this.selectedChildId);

    if (!selectedDoctor || !selectedChild) {
      this.error = 'Invalid selection';
      return;
    }

    this.doctorSelected.emit({
      doctor: selectedDoctor,
      child: selectedChild,
      subject: this.subject.trim()
    });
  }

  isFormValid(): boolean {
    return !!(this.selectedDoctorId && this.selectedChildId && this.subject.trim());
  }

  getCharacterCount(): number {
    return this.subject.length;
  }
}
