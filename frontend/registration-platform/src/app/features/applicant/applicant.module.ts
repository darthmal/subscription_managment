import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { ApplicantRoutingModule } from './applicant-routing.module';
import { ApplicantDashboardComponent } from './applicant-dashboard/applicant-dashboard.component';

@NgModule({
  declarations: [
    ApplicantDashboardComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ApplicantRoutingModule
  ]
})
export class ApplicantModule { }
