import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-applicant-dashboard',
  templateUrl: './applicant-dashboard.component.html',
  styleUrl: './applicant-dashboard.component.scss',
  imports: [CommonModule, FormsModule],
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

  constructor() {}

  ngOnInit(): void {}

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
