import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PersonalInfoService, PersonalInfo } from '../../../core/services/personal-info.service';
import { DocumentService, Document, DocumentType } from '../../../core/services/document.service';

@Component({
  selector: 'app-applicant-dashboard',
  templateUrl: './applicant-dashboard.component.html',
  styleUrl: './applicant-dashboard.component.scss',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  standalone: true
})
export class ApplicantDashboardComponent implements OnInit {
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

  constructor(
    private formBuilder: FormBuilder,
    private personalInfoService: PersonalInfoService,
    private documentService: DocumentService
  ) {}

  ngOnInit(): void {
    this.initPersonalInfoForm();
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
    
    // Load documents when navigating to the documents step
    if (this.currentStep === 2) {
      this.loadDocuments();
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
      
      // Load documents when navigating to the documents step
      if (this.currentStep === 2) {
        this.loadDocuments();
      }
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      this.updateStepsStatus();
      
      // Load documents when navigating to the documents step
      if (this.currentStep === 2) {
        this.loadDocuments();
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
    if (input.files && input.files.length) {
      this.selectedFile = input.files[0];
      
      // Create preview for image files
      if (this.isImageFile(this.selectedFile)) {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.selectedFilePreview = e.target?.result as string;
        };
        reader.readAsDataURL(this.selectedFile);
      } else {
        this.selectedFilePreview = null;
      }
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
}
