import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, UserSummaryDTO, PageResponse } from '../../../core/services/admin.service';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
})
export class UserManagementComponent implements OnInit {
  // Make Math available to the template
  Math = Math;
  users: UserSummaryDTO[] = [];
  loading = false;
  error = '';
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Sorting
  sortField = 'createdAt';
  sortDirection = 'desc';
  
  // Filtering
  searchTerm = '';
  
  constructor(
    private adminService: AdminService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.loadUsers();
  }
  
  loadUsers(): void {
    this.loading = true;
    this.error = '';
    
    const sort = `${this.sortField},${this.sortDirection}`;
    
    this.adminService.getUsers(this.currentPage, this.pageSize, sort).subscribe({
      next: (response: PageResponse<UserSummaryDTO>) => {
        this.users = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load users. Please try again.';
        this.loading = false;
        console.error('Error loading users:', err);
      }
    });
  }
  
  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }
  
  onSortChange(field: string): void {
    if (this.sortField === field) {
      // Toggle direction if clicking the same field
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.loadUsers();
  }
  
  getSortIcon(field: string): string {
    if (this.sortField !== field) {
      return 'fa-sort';
    }
    return this.sortDirection === 'asc' ? 'fa-sort-up' : 'fa-sort-down';
  }
  
  viewUserApplication(userId: number): void {
    this.router.navigate(['/admin/users', userId]);
  }
  
  getRoleClasses(role: string): string {
    switch (role) {
      case 'ROLE_ADMIN':
        return 'bg-purple-100 text-purple-800';
      case 'ROLE_APPLICANT':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
  
  getStatusClasses(enabled: boolean, locked: boolean): string {
    if (!enabled) {
      return 'bg-gray-100 text-gray-800';
    }
    if (locked) {
      return 'bg-red-100 text-red-800';
    }
    return 'bg-green-100 text-green-800';
  }
  
  getStatusText(enabled: boolean, locked: boolean): string {
    if (!enabled) {
      return 'Disabled';
    }
    if (locked) {
      return 'Locked';
    }
    return 'Active';
  }
  
  get pages(): number[] {
    const result: number[] = [];
    const maxVisiblePages = 5;
    
    if (this.totalPages <= maxVisiblePages) {
      // Show all pages if there are few
      for (let i = 0; i < this.totalPages; i++) {
        result.push(i);
      }
    } else {
      // Show a subset of pages with the current page in the middle if possible
      let start = Math.max(0, this.currentPage - Math.floor(maxVisiblePages / 2));
      let end = Math.min(this.totalPages - 1, start + maxVisiblePages - 1);
      
      // Adjust start if we're near the end
      if (end === this.totalPages - 1) {
        start = Math.max(0, end - maxVisiblePages + 1);
      }
      
      for (let i = start; i <= end; i++) {
        result.push(i);
      }
    }
    
    return result;
  }
}
