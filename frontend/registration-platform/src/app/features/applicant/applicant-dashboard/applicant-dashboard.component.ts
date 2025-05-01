import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PersonalInfoService, PersonalInfo } from '../../../core/services/personal-info.service';
import { DocumentService, Document, DocumentType } from '../../../core/services/document.service';
import { AcademicHistoryService, AcademicHistory } from '../../../core/services/academic-history.service';
import { ContactInfoService, ContactInfo } from '../../../core/services/contact-info.service';

@Component({
  selector: 'app-applicant-dashboard',
  templateUrl: './applicant-dashboard.component.html',
  styleUrl: './applicant-dashboard.component.scss',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  standalone: true
})
export class ApplicantDashboardComponent implements OnInit {
  // Make Math available to the template
  Math = Math;
  currentStep: number = 1;
  totalSteps: number = 4;
  
  steps = [
    { id: 1, name: 'Personal Information', completed: false, active: true },
    { id: 2, name: 'Documents', completed: false, active: false },
    { id: 3, name: 'Academic History', completed: false, active: false },
    { id: 4, name: 'Contact Information', completed: false, active: false }
  ];

  // Personal Information Form
  personalInfoForm!: FormGroup;
  isLoadingPersonalInfo = false;
  isSavingPersonalInfo = false;
  personalInfoErrorMessage = '';
  personalInfoSuccessMessage = '';
  genderOptions = [
    { value: 'MALE', label: 'Male' },
    { value: 'FEMALE', label: 'Female' },
    { value: 'OTHER', label: 'Other' }
  ];
  idDocumentTypeOptions = [
    { value: 'NATIONAL_ID_CARD', label: 'National ID Card' },
    { value: 'PASSPORT', label: 'Passport' },
    { value: 'DRIVERS_LICENSE', label: 'Driver\'s License' },
    { value: 'OTHER', label: 'Other' }
  ];

  // Document Management
  documents: Document[] = [];
  isLoadingDocuments = false;
  isUploadingDocument = false;
  documentErrorMessage = '';
  documentSuccessMessage = '';
  selectedFile: File | null = null;
  selectedDocumentType: DocumentType | '' = '';
  selectedFilePreview: string | null = null;
  uploadProgress = 0;
  @ViewChild('documentFileInput') documentFileInput!: ElementRef<HTMLInputElement>;
  @ViewChild('documentList') documentList!: ElementRef<HTMLElement>;
  documentTypes = [
    { value: DocumentType.ID_PHOTO, label: 'ID Photo', description: 'A recent passport-style photo (JPEG, PNG, max 1MB)', allowedTypes: ['image/jpeg', 'image/png'], maxSize: 1024 * 1024 },
    { value: DocumentType.ID_CARD_FRONT, label: 'ID Card (Front)', description: 'Front side of your national ID card (JPEG, PNG, PDF, max 2MB)', allowedTypes: ['image/jpeg', 'image/png', 'application/pdf'], maxSize: 2 * 1024 * 1024 },
    { value: DocumentType.ID_CARD_BACK, label: 'ID Card (Back)', description: 'Back side of your national ID card (JPEG, PNG, PDF, max 2MB)', allowedTypes: ['image/jpeg', 'image/png', 'application/pdf'], maxSize: 2 * 1024 * 1024 },
    { value: DocumentType.PASSPORT, label: 'Passport', description: 'Main page of your passport (JPEG, PNG, PDF, max 2MB)', allowedTypes: ['image/jpeg', 'image/png', 'application/pdf'], maxSize: 2 * 1024 * 1024 },
    { value: DocumentType.DIPLOMA_BAC, label: 'High School Diploma', description: 'Your high school diploma or equivalent (PDF, max 5MB)', allowedTypes: ['application/pdf'], maxSize: 5 * 1024 * 1024 },
    { value: DocumentType.TRANSCRIPT, label: 'Academic Transcript', description: 'Your academic transcript (PDF, max 5MB)', allowedTypes: ['application/pdf'], maxSize: 5 * 1024 * 1024 },
    { value: DocumentType.MOTIVATION_LETTER, label: 'Motivation Letter', description: 'A letter explaining your motivation (PDF, max 2MB)', allowedTypes: ['application/pdf'], maxSize: 2 * 1024 * 1024 },
    { value: DocumentType.RECOMMENDATION_LETTER, label: 'Recommendation Letter', description: 'A letter of recommendation (PDF, max 2MB)', allowedTypes: ['application/pdf'], maxSize: 2 * 1024 * 1024 },
    { value: DocumentType.OTHER, label: 'Other Document', description: 'Any other relevant document (PDF, max 5MB)', allowedTypes: ['application/pdf'], maxSize: 5 * 1024 * 1024 }
  ];

  // Academic History
  academicHistoryForm!: FormGroup;
  academicHistories: AcademicHistory[] = [];
  isLoadingAcademicHistory = false;
  isSavingAcademicHistory = false;
  isEditingAcademicHistory = false;
  editingAcademicHistoryId?: number;
  academicHistoryErrorMessage = '';
  academicHistorySuccessMessage = '';

  // Contact Information
  contactInfoForm!: FormGroup;
  isLoadingContactInfo = false;
  isSavingContactInfo = false;
  contactInfoErrorMessage = '';
  contactInfoSuccessMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private personalInfoService: PersonalInfoService,
    private documentService: DocumentService,
    private academicHistoryService: AcademicHistoryService,
    private contactInfoService: ContactInfoService
  ) {}

  ngOnInit(): void {
    this.initPersonalInfoForm();
    this.initAcademicHistoryForm();
    this.initContactInfoForm();
    this.loadPersonalInfo();
    this.loadDocuments();
  }

  initPersonalInfoForm(): void {
    this.personalInfoForm = this.formBuilder.group({
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      firstNames: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      gender: ['', [Validators.required]],
      dateOfBirth: ['', [Validators.required]],
      nationality: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      idDocumentType: ['', [Validators.required]]
    });
  }

  loadPersonalInfo(): void {
    this.isLoadingPersonalInfo = true;
    this.personalInfoErrorMessage = '';

    this.personalInfoService.getPersonalInfo().subscribe({
      next: (data) => {
        this.personalInfoForm.patchValue({
          lastName: data.lastName,
          firstNames: data.firstNames,
          gender: data.gender,
          dateOfBirth: data.dateOfBirth,
          nationality: data.nationality,
          idDocumentType: data.idDocumentType
        });
        this.steps[0].completed = true;
        this.updateStepsStatus();
        this.isLoadingPersonalInfo = false;
      },
      error: (error) => {
        // 404 is expected if the user hasn't saved personal info yet
        if (error.message !== 'No personal information found') {
          this.personalInfoErrorMessage = error.message;
        }
        this.isLoadingPersonalInfo = false;
      }
    });
  }

  savePersonalInfo(): void {
    if (this.personalInfoForm.invalid) {
      this.personalInfoForm.markAllAsTouched();
      return;
    }

    this.isSavingPersonalInfo = true;
    this.personalInfoErrorMessage = '';
    this.personalInfoSuccessMessage = '';

    const personalInfo: PersonalInfo = this.personalInfoForm.value;

    this.personalInfoService.savePersonalInfo(personalInfo).subscribe({
      next: () => {
        this.personalInfoSuccessMessage = 'Personal information saved successfully!';
        this.steps[0].completed = true;
        this.updateStepsStatus();
        setTimeout(() => this.nextStep(), 1000);
      },
      error: (error) => {
        this.personalInfoErrorMessage = error.message;
      },
      complete: () => {
        this.isSavingPersonalInfo = false;
      }
    });
  }

  // Helper method to check if a form control is invalid and touched
  isInvalidAndTouched(controlName: string): boolean {
    const control = this.personalInfoForm.get(controlName);
    return !!control && control.invalid && control.touched;
  }

  // Helper method to get error message for a form control
  getErrorMessage(controlName: string): string {
    const control = this.personalInfoForm.get(controlName);
    if (!control) return '';
    
    if (control.errors?.['required']) {
      return 'This field is required';
    }
    if (control.errors?.['minlength']) {
      return `Minimum length is ${control.errors['minlength'].requiredLength} characters`;
    }
    if (control.errors?.['maxlength']) {
      return `Maximum length is ${control.errors['maxlength'].requiredLength} characters`;
    }
    if (control.errors?.['email']) {
      return 'Please enter a valid email address';
    }
    
    return 'Invalid input';
  }

  setStep(step: number): void {
    this.currentStep = step;
    this.updateStepsStatus();
    
    // Load data based on the current step
    if (this.currentStep === 2) {
      this.loadDocuments();
    } else if (this.currentStep === 3) {
      this.loadAcademicHistory();
    } else if (this.currentStep === 4) {
      this.loadContactInfo();
    }
  }

  updateStepsStatus(): void {
    this.steps.forEach(step => {
      step.active = step.id === this.currentStep;
      step.completed = step.id < this.currentStep;
    });
  }

  nextStep(): void {
    if (this.currentStep < this.totalSteps) {
      // If we're on the documents step and have a selected file but haven't uploaded it yet,
      // prompt the user to upload it first
      if (this.currentStep === 2 && this.selectedFile && this.selectedDocumentType) {
        if (confirm('You have a selected document that has not been uploaded. Would you like to upload it before proceeding?')) {
          this.uploadDocument();
          return;
        }
      }
      
      this.currentStep++;
      this.updateStepsStatus();
      
      // Load data based on the current step
      if (this.currentStep === 2) {
        this.loadDocuments();
      } else if (this.currentStep === 3) {
        this.loadAcademicHistory();
      } else if (this.currentStep === 4) {
        this.loadContactInfo();
      }
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      this.updateStepsStatus();
      
      // Load data based on the current step
      if (this.currentStep === 2) {
        this.loadDocuments();
      } else if (this.currentStep === 3) {
        this.loadAcademicHistory();
      } else if (this.currentStep === 4) {
        this.loadContactInfo();
      }
    }
  }

  // Document Management Methods
  loadDocuments(): void {
    this.isLoadingDocuments = true;
    this.documentErrorMessage = '';

    this.documentService.getDocuments().subscribe({
      next: (documents) => {
        this.documents = documents;
        // Mark step as completed if at least one document is uploaded
        if (documents.length > 0) {
          this.steps[1].completed = true;
          this.updateStepsStatus();
        }
      },
      error: (error) => {
        this.documentErrorMessage = error.message;
        this.isLoadingDocuments = false;
      },
      complete: () => {
        this.isLoadingDocuments = false;
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      
      // Create preview for image files
      if (this.isImageFile(this.selectedFile)) {
        const reader = new FileReader();
        reader.onload = () => {
          this.selectedFilePreview = reader.result as string;
        };
        reader.readAsDataURL(this.selectedFile);
      }
    }
  }
  
  clearSelectedFile(): void {
    this.selectedFile = null;
    this.selectedFilePreview = null;
    if (this.documentFileInput) {
      this.documentFileInput.nativeElement.value = '';
    }
  }

  isImageFile(file: File): boolean {
    return file.type.startsWith('image/');
  }

  uploadDocument(): void {
    if (!this.selectedFile || !this.selectedDocumentType) {
      this.documentErrorMessage = 'Please select both a file and document type';
      return;
    }

    // Validate file type and size
    const documentTypeInfo = this.documentTypes.find(dt => dt.value === this.selectedDocumentType);
    if (documentTypeInfo) {
      if (!documentTypeInfo.allowedTypes.includes(this.selectedFile.type)) {
        this.documentErrorMessage = `Invalid file type. Allowed types for ${documentTypeInfo.label}: ${documentTypeInfo.allowedTypes.join(', ')}`;
        return;
      }

      if (this.selectedFile.size > documentTypeInfo.maxSize) {
        this.documentErrorMessage = `File size exceeds the limit of ${documentTypeInfo.maxSize / (1024 * 1024)}MB for ${documentTypeInfo.label}`;
        return;
      }
    }

    this.isUploadingDocument = true;
    this.documentErrorMessage = '';
    this.documentSuccessMessage = '';

    this.documentService.uploadDocument(this.selectedFile, this.selectedDocumentType as DocumentType).subscribe({
      next: (document) => {
        // Mark the step as completed
        this.steps[1].completed = true;
        this.updateStepsStatus();
        
        // Show success message with document details
        this.documentSuccessMessage = `${document.originalFilename} uploaded successfully as ${this.getDocumentTypeLabel(document.documentType)}!`;
        
        // Refresh the document list
        this.loadDocuments();
        
        // Reset form
        this.selectedFile = null;
        this.selectedFilePreview = null;
        this.selectedDocumentType = '';
        
        // Reset file input
        if (this.documentFileInput) {
          this.documentFileInput.nativeElement.value = '';
        }
        
        // Auto-scroll to the document list
        setTimeout(() => {
          if (this.documentList) {
            this.documentList.nativeElement.scrollIntoView({ behavior: 'smooth' });
          }
        }, 500);
      },
      error: (error) => {
        this.documentErrorMessage = error.message;
        this.isUploadingDocument = false;
      },
      complete: () => {
        this.isUploadingDocument = false;
      }
    });
  }

  downloadDocument(id: number, filename: string): void {
    this.documentService.downloadDocument(id).subscribe({
      next: (event: any) => {
        if (event.type === 4) { // HttpEventType.Response = 4
          const blob = new Blob([event.body], { type: event.headers.get('Content-Type') || 'application/octet-stream' });
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          window.URL.revokeObjectURL(url);
          document.body.removeChild(a);
        }
      },
      error: (error) => {
        this.documentErrorMessage = error.message;
      }
    });
  }

  deleteDocument(id: number): void {
    if (confirm('Are you sure you want to delete this document?')) {
      this.documentService.deleteDocument(id).subscribe({
        next: () => {
          this.documentSuccessMessage = 'Document deleted successfully';
          this.loadDocuments(); // Refresh the document list
        },
        error: (error) => {
          this.documentErrorMessage = error.message;
        }
      });
    }
  }

  getDocumentStatusClass(status: string): string {
    switch (status) {
      case 'VALIDATED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'UPLOADED':
      default:
        return 'bg-yellow-100 text-yellow-800';
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) {
      return bytes + ' B';
    } else if (bytes < 1024 * 1024) {
      return (bytes / 1024).toFixed(1) + ' KB';
    } else {
      return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }
  }

  getDocumentTypeLabel(type: string): string {
    const docType = this.documentTypes.find(dt => dt.value === type);
    return docType ? docType.label : type;
  }
  
  getSelectedDocumentTypeDescription(): string {
    if (!this.selectedDocumentType) return '';
    const docType = this.documentTypes.find(dt => dt.value === this.selectedDocumentType);
    return docType ? docType.description : '';
  }
  
  isCurrentStep(step: number): boolean {
    console.log('Current step:', this.currentStep, 'Requested step:', step);
    return this.currentStep === step;
  }

  // Academic History Methods
  initAcademicHistoryForm(): void {
    this.academicHistoryForm = this.formBuilder.group({
      institutionName: ['', [Validators.required]],
      specialization: ['', [Validators.required]],
      startDate: ['', [Validators.required]],
      endDate: [''],
    });
  }

  loadAcademicHistory(): void {
    this.isLoadingAcademicHistory = true;
    this.academicHistoryErrorMessage = '';

    this.academicHistoryService.getAcademicHistory().subscribe({
      next: (data) => {
        this.academicHistories = data;
        // Mark step as completed if at least one academic history is added
        if (data.length > 0) {
          this.steps[2].completed = true;
          this.updateStepsStatus();
        }
        this.isLoadingAcademicHistory = false;
      },
      error: (error) => {
        // 404 is expected if the user hasn't saved academic history yet
        if (error.message !== 'No academic history found') {
          this.academicHistoryErrorMessage = error.message;
        }
        this.isLoadingAcademicHistory = false;
      }
    });
  }

  saveAcademicHistory(): void {
    if (this.academicHistoryForm.invalid) {
      this.academicHistoryForm.markAllAsTouched();
      return;
    }

    this.isSavingAcademicHistory = true;
    this.academicHistoryErrorMessage = '';
    this.academicHistorySuccessMessage = '';

    const academicHistory: AcademicHistory = this.academicHistoryForm.value;
    
    // Format dates properly
    if (academicHistory.startDate) {
      academicHistory.startDate = new Date(academicHistory.startDate).toISOString().split('T')[0];
    }
    
    if (academicHistory.endDate) {
      academicHistory.endDate = new Date(academicHistory.endDate).toISOString().split('T')[0];
    }

    if (this.isEditingAcademicHistory && this.editingAcademicHistoryId) {
      // Update existing academic history
      this.academicHistoryService.updateAcademicHistory(this.editingAcademicHistoryId, academicHistory).subscribe({
        next: (updatedHistory) => {
          this.academicHistorySuccessMessage = 'Academic history updated successfully!';
          this.loadAcademicHistory();
          this.resetAcademicHistoryForm();
          this.steps[2].completed = true;
          this.updateStepsStatus();
        },
        error: (error) => {
          this.academicHistoryErrorMessage = error.message;
          this.isSavingAcademicHistory = false;
        },
        complete: () => {
          this.isSavingAcademicHistory = false;
        }
      });
    } else {
      // Add new academic history
      this.academicHistoryService.addAcademicHistory(academicHistory).subscribe({
        next: (newHistory) => {
          this.academicHistorySuccessMessage = 'Academic history added successfully!';
          this.loadAcademicHistory();
          this.resetAcademicHistoryForm();
          this.steps[2].completed = true;
          this.updateStepsStatus();
        },
        error: (error) => {
          this.academicHistoryErrorMessage = error.message;
          this.isSavingAcademicHistory = false;
        },
        complete: () => {
          this.isSavingAcademicHistory = false;
        }
      });
    }
  }

  editAcademicHistory(history: AcademicHistory): void {
    this.isEditingAcademicHistory = true;
    this.editingAcademicHistoryId = history.id;
    
    this.academicHistoryForm.patchValue({
      institutionName: history.institutionName,
      specialization: history.specialization,
      startDate: history.startDate,
      endDate: history.endDate || ''
    });
  }

  deleteAcademicHistory(id: number): void {
    if (confirm('Are you sure you want to delete this academic history?')) {
      this.academicHistoryService.deleteAcademicHistory(id).subscribe({
        next: () => {
          this.academicHistorySuccessMessage = 'Academic history deleted successfully!';
          this.loadAcademicHistory();
        },
        error: (error) => {
          this.academicHistoryErrorMessage = error.message;
        }
      });
    }
  }

  resetAcademicHistoryForm(): void {
    this.academicHistoryForm.reset();
    this.isEditingAcademicHistory = false;
    this.editingAcademicHistoryId = undefined;
  }

  // Contact Information Methods
  initContactInfoForm(): void {
    this.contactInfoForm = this.formBuilder.group({
      phoneNumber: ['', [Validators.required]],
      address: this.formBuilder.group({
        street: ['', [Validators.required]],
        street2: [''],
        city: ['', [Validators.required]],
        postalCode: ['', [Validators.required]],
        country: ['', [Validators.required]]
      }),
      emergencyContact: this.formBuilder.group({
        name: ['', [Validators.required]],
        relationship: ['', [Validators.required]],
        phone: ['', [Validators.required]]
      })
    });
  }

  loadContactInfo(): void {
    this.isLoadingContactInfo = true;
    this.contactInfoErrorMessage = '';

    this.contactInfoService.getContactInfo().subscribe({
      next: (data) => {
        this.contactInfoForm.patchValue(data);
        this.steps[3].completed = true;
        this.updateStepsStatus();
        this.isLoadingContactInfo = false;
      },
      error: (error) => {
        // 404 is expected if the user hasn't saved contact info yet
        if (error.message !== 'No contact information found') {
          this.contactInfoErrorMessage = error.message;
        }
        this.isLoadingContactInfo = false;
      }
    });
  }

  saveContactInfo(): void {
    if (this.contactInfoForm.invalid) {
      this.contactInfoForm.markAllAsTouched();
      return;
    }

    this.isSavingContactInfo = true;
    this.contactInfoErrorMessage = '';
    this.contactInfoSuccessMessage = '';

    const contactInfo: ContactInfo = this.contactInfoForm.value;

    this.contactInfoService.saveContactInfo(contactInfo).subscribe({
      next: () => {
        this.contactInfoSuccessMessage = 'Contact information saved successfully!';
        this.steps[3].completed = true;
        this.updateStepsStatus();
      },
      error: (error) => {
        this.contactInfoErrorMessage = error.message;
      },
      complete: () => {
        this.isSavingContactInfo = false;
      }
    });
  }
}
