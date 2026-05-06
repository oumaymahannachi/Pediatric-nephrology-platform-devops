import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { AdminService } from '../../../../core/services/admin.service';
import { UserProfile } from '../../../../core/models/auth.models';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements OnInit {
  currentUserId = '';
  loading = true;
  users: UserProfile[] = [];
  filteredUsers: UserProfile[] = [];
  searchQuery = '';
  roleFilter = '';

  currentPage = 1;
  pageSize = 8;

  showEditModal = false;
  editingUser: UserProfile | null = null;
  editForm!: FormGroup;
  editSaving = false;

  alertMsg = '';
  alertType: 'success' | 'error' = 'success';

  constructor(
    private auth: AuthService,
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.editForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      cin: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      phone: [''],
      specialization: [''],
      serviceUnit: ['']
    });
  }

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(u => { if (u) this.currentUserId = u.id; });
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.getAllUsers().subscribe({
      next: u => {
        this.users = u;
        this.applyFilters();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  applyFilters(): void {
    let list = this.users.filter(u => u.id !== this.currentUserId);
    if (this.roleFilter) list = list.filter(u => u.role === this.roleFilter);
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      list = list.filter(u =>
        u.fullName.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        (u.cin && u.cin.includes(q))
      );
    }
    this.filteredUsers = list;
    this.currentPage = 1;
  }

  get totalPages(): number { return Math.ceil(this.filteredUsers.length / this.pageSize); }

  get paginatedUsers(): UserProfile[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredUsers.slice(start, start + this.pageSize);
  }

  get pageNumbers(): number[] { return Array.from({ length: this.totalPages }, (_, i) => i + 1); }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) this.currentPage = page;
  }

  banUser(id: string): void {
    this.adminService.banUser(id).subscribe({
      next: () => { const u = this.users.find(x => x.id === id); if (u) u.status = 'BANNED'; this.applyFilters(); this.showAlert('User banned', 'success'); },
      error: () => this.showAlert('Failed to ban user', 'error')
    });
  }

  unbanUser(id: string): void {
    this.adminService.unbanUser(id).subscribe({
      next: () => { const u = this.users.find(x => x.id === id); if (u) u.status = 'ACTIVE'; this.applyFilters(); this.showAlert('User unbanned', 'success'); },
      error: () => this.showAlert('Failed to unban user', 'error')
    });
  }

  openEditUser(user: UserProfile): void {
    this.editingUser = user;
    this.editForm.patchValue({
      fullName: user.fullName, email: user.email || '', cin: user.cin || '',
      phone: user.phone || '', specialization: user.specialization || '', serviceUnit: user.serviceUnit || ''
    });
    this.showEditModal = true;
  }

  saveEditUser(): void {
    if (!this.editingUser || this.editForm.invalid) return;
    this.editSaving = true;
    this.adminService.updateUser(this.editingUser.id!, this.editForm.value).subscribe({
      next: updated => {
        const idx = this.users.findIndex(u => u.id === this.editingUser!.id);
        if (idx >= 0) this.users[idx] = { ...this.users[idx], ...updated };
        this.applyFilters();
        this.showEditModal = false;
        this.editSaving = false;
        this.showAlert('User updated', 'success');
      },
      error: () => { this.editSaving = false; this.showAlert('Failed to update user', 'error'); }
    });
  }

  deleteUser(id: string): void {
    if (!confirm('Delete this user permanently?')) return;
    this.adminService.deleteUser(id).subscribe({
      next: () => { this.users = this.users.filter(u => u.id !== id); this.applyFilters(); this.showAlert('User deleted', 'success'); },
      error: () => this.showAlert('Failed to delete user', 'error')
    });
  }

  getRoleBadgeClass(role: string): string {
    const m: Record<string, string> = { ADMIN: 'admin', DOCTOR: 'doctor', PARENT: 'parent', INFIRMIER: 'nurse' };
    return m[role] || 'default';
  }

  getStatusClass(status: string): string { return status === 'ACTIVE' ? 'active' : 'banned'; }

  private showAlert(msg: string, type: 'success' | 'error'): void {
    this.alertMsg = msg; this.alertType = type;
    setTimeout(() => this.alertMsg = '', 4000);
  }
}
