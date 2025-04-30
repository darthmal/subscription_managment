import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss',
  imports: [CommonModule, DatePipe],
  standalone: true
})
export class AdminDashboardComponent implements OnInit {
  // Mock data for dashboard statistics
  statistics = {
    totalApplications: 256,
    pendingReview: 42,
    approved: 198,
    rejected: 16,
    completionRate: 78
  };

  // Mock data for recent applications
  recentApplications = [
    { id: 1, name: 'John Doe', email: 'john.doe@example.com', status: 'PENDING', submittedAt: '2025-04-28T10:30:00' },
    { id: 2, name: 'Jane Smith', email: 'jane.smith@example.com', status: 'APPROVED', submittedAt: '2025-04-27T14:45:00' },
    { id: 3, name: 'Robert Johnson', email: 'robert.j@example.com', status: 'REJECTED', submittedAt: '2025-04-26T09:15:00' },
    { id: 4, name: 'Emily Davis', email: 'emily.d@example.com', status: 'PENDING', submittedAt: '2025-04-25T16:20:00' },
    { id: 5, name: 'Michael Wilson', email: 'michael.w@example.com', status: 'APPROVED', submittedAt: '2025-04-24T11:10:00' }
  ];

  constructor() {}

  ngOnInit(): void {}

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
