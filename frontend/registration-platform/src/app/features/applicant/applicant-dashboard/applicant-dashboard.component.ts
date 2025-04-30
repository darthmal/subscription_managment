import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PersonalInfoService, PersonalInfo } from '../../../core/services/personal-info.service';

@Component({
  selector: 'app-applicant-dashboard',
  templateUrl: './applicant-dashboard.component.html',
  styleUrl: './applicant-dashboard.component.scss',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  standalone: true
})
export class ApplicantDashboardComponent implements OnInit {
  currentStep = 1;
  totalSteps = 4;
  
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

  constructor(
    private formBuilder: FormBuilder,
    private personalInfoService: PersonalInfoService
  ) {}

  ngOnInit(): void {
    this.initPersonalInfoForm();
    this.loadPersonalInfo();
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
      },
      error: (error) => {
        // 404 is expected if the user hasn't saved personal info yet
        if (error.message !== 'No personal information found') {
          this.personalInfoErrorMessage = error.message;
        }
      },
      complete: () => {
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
  }

  updateStepsStatus(): void {
    this.steps.forEach(step => {
      step.active = step.id === this.currentStep;
      step.completed = step.id < this.currentStep;
    });
  }

  nextStep(): void {
    if (this.currentStep < this.totalSteps) {
      this.currentStep++;
      this.updateStepsStatus();
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      this.updateStepsStatus();
    }
  }
}
