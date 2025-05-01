import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService, UserSummaryDTO, PageResponse } from '../../../core/services/admin.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  imports: [CommonModule, DatePipe, RouterModule],
  standalone: true
})
export class AdminDashboardComponent implements OnInit {
  // Make Math available to the template
  Math = Math;
  currentDate = new Date();
  // Dashboard statistics
  statistics = {
    totalApplications: 0,
    pendingReview: 0,
    approved: 0,
    rejected: 0,
    completionRate: 0
  };

  // Recent applications
  recentApplications: UserSummaryDTO[] = [];
  
  // Loading states
  loadingStatistics = false;
  loadingApplications = false;
  error = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadDashboardData();
    this.loadRecentApplications();
  }
  
  loadDashboardData(): void {
    this.loadingStatistics = true;
    
    // Use forkJoin to make parallel API calls
    forkJoin({
      total: this.adminService.getTotalApplicationsCount(),
      pending: this.adminService.getPendingApplicationsCount(),
      approved: this.adminService.getApprovedApplicationsCount(),
      rejected: this.adminService.getRejectedApplicationsCount(),
      completionRate: this.adminService.getCompletionRate()
    }).subscribe({
      next: (data) => {
        this.statistics = {
          totalApplications: data.total,
          pendingReview: data.pending,
          approved: data.approved,
          rejected: data.rejected,
          completionRate: data.completionRate
        };
        this.loadingStatistics = false;
      },
      error: (err) => {
        console.error('Error loading dashboard statistics:', err);
        this.error = 'Failed to load dashboard statistics';
        this.loadingStatistics = false;
      }
    });
  }
  
  loadRecentApplications(): void {
    this.loadingApplications = true;
    
    // Get the first page with 5 most recent applications
    this.adminService.getUsers(0, 5, 'createdAt,desc').subscribe({
      next: (response: PageResponse<UserSummaryDTO>) => {
        this.recentApplications = response.content;
        this.loadingApplications = false;
      },
      error: (err) => {
        console.error('Error loading recent applications:', err);
        this.error = 'Failed to load recent applications';
        this.loadingApplications = false;
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}
