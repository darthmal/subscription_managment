import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { ApplicationDetailDTO, DocumentDTO, DocumentStatusUpdateDTO } from '../../../core/services/admin.service';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-user-application',
  templateUrl: './user-application.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
})
export class UserApplicationComponent implements OnInit {
  userId!: number;
  applicationDetail: ApplicationDetailDTO | null = null;
  loading = false;
  error = '';
  successMessage = '';
  
  // Document validation
  selectedDocument: DocumentDTO | null = null;
  showDocumentModal = false;
  modalAction: 'APPROVED' | 'REJECTED' = 'APPROVED';
  documentNotes = '';
  updateInProgress = false;
  showSuccessNotification = false;
  showErrorNotification = false;
  errorMessage = '';
  
  // Application status update
  newApplicationStatus = '';
  updatingApplicationStatus = false;
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminService
  ) {}
  
  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.userId = +params['id'];
      this.loadApplicationDetail();
    });
  }
  
  loadApplicationDetail(): void {
    this.loading = true;
    this.error = '';
    
    this.adminService.getUserApplication(this.userId).subscribe({
      next: (data) => {
        this.applicationDetail = data;
        this.loading = false;
        
        // Set default application status from the data
        if (data.userSummary.applicationStatus) {
          this.newApplicationStatus = data.userSummary.applicationStatus;
        }
      },
      error: (err) => {
        this.error = 'Failed to load application details. Please try again.';
        this.loading = false;
        console.error('Error loading application details:', err);
      }
    });
  }
  
  openDocumentModal(document: DocumentDTO, action: 'APPROVED' | 'REJECTED'): void {
    this.selectedDocument = document;
    this.modalAction = action;
    this.documentNotes = document.validationNotes || '';
    this.showDocumentModal = true;
  }
  
  closeDocumentModal(): void {
    this.showDocumentModal = false;
    this.selectedDocument = null;
    this.documentNotes = '';
  }
  
  downloadDocument(document: DocumentDTO): void {
    // In a real application, this would trigger a download
    // For now, we'll just show a success notification
    this.successMessage = `Document ${document.fileName} would be downloaded in a real application`;
    this.showSuccessNotification = true;
    
    // Clear success message after 3 seconds
    setTimeout(() => {
      this.showSuccessNotification = false;
    }, 3000);
  }
  
  updateDocumentStatus(): void {
    if (!this.selectedDocument) return;
    
    this.updateInProgress = true;
    const statusUpdate: DocumentStatusUpdateDTO = {
      newStatus: this.modalAction,
      validationNotes: this.documentNotes
    };
    
    this.adminService.updateDocumentStatus(this.selectedDocument.id, statusUpdate)
      .pipe(finalize(() => this.updateInProgress = false))
      .subscribe({
        next: (updatedDocument) => {
          // Update the document in the list
          if (this.applicationDetail && this.applicationDetail.documents) {
            const index = this.applicationDetail.documents.findIndex(d => d.id === updatedDocument.id);
            if (index !== -1) {
              this.applicationDetail.documents[index] = updatedDocument;
            }
          }
          
          this.successMessage = `Document successfully ${this.modalAction === 'APPROVED' ? 'validated' : 'rejected'}`;
          this.showSuccessNotification = true;
          this.closeDocumentModal();
          
          // Clear success message after 3 seconds
          setTimeout(() => {
            this.showSuccessNotification = false;
          }, 3000);
        },
        error: (err) => {
          this.errorMessage = `Failed to ${this.modalAction === 'APPROVED' ? 'validate' : 'reject'} document. Please try again.`;
          this.showErrorNotification = true;
          console.error('Error updating document status:', err);
          
          // Clear error message after 5 seconds
          setTimeout(() => {
            this.showErrorNotification = false;
          }, 5000);
        }
      });
  }
  
  updateApplicationStatus(): void {
    if (!this.newApplicationStatus) return;
    
    this.updatingApplicationStatus = true;
    
    this.adminService.updateApplicationStatus(this.userId, this.newApplicationStatus)
      .pipe(finalize(() => this.updatingApplicationStatus = false))
      .subscribe({
        next: (updatedUser) => {
          if (this.applicationDetail) {
            this.applicationDetail.userSummary = updatedUser;
          }
          
          this.successMessage = `Application status updated to ${updatedUser.applicationStatus}`;
          this.showSuccessNotification = true;
          
          // Clear success message after 3 seconds
          setTimeout(() => {
            this.showSuccessNotification = false;
          }, 3000);
        },
        error: (err) => {
          this.errorMessage = 'Failed to update application status. Please try again.';
          this.showErrorNotification = true;
          console.error('Error updating application status:', err);
          
          // Clear error message after 5 seconds
          setTimeout(() => {
            this.showErrorNotification = false;
          }, 5000);
        }
      });
  }
  
  getStatusClass(status: string): string {
    switch (status) {
      case 'VALIDATED':
      case 'APPROVED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'PENDING_REVIEW':
      case 'UPLOADED':
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
  
  getApplicationStatusClass(status: string | undefined): string {
    if (!status) return 'bg-gray-100 text-gray-800';
    
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
  
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
  
  goBack(): void {
    this.router.navigate(['/admin/users']);
  }
}
