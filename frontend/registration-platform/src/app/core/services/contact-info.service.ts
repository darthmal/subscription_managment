import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Address {
  street: string;
  street2?: string;
  city: string;
  postalCode: string;
  country: string;
  latitude?: number | null;
  longitude?: number | null;
}

export interface EmergencyContact {
  name: string;
  relationship: string;
  phone: string;
}

export interface ContactInfo {
  emailVerified?: boolean;
  phoneNumber: string;
  address: Address;
  emergencyContact: EmergencyContact;
}

@Injectable({
  providedIn: 'root'
})
export class ContactInfoService {
  private readonly API_URL = `${environment.apiUrl}/api/applicant/contact-info`;

  constructor(private http: HttpClient) {}

  getContactInfo(): Observable<ContactInfo> {
    return this.http.get<ContactInfo>(this.API_URL)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to retrieve contact information';
          
          if (error.status === 404) {
            errorMessage = 'No contact information found';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  saveContactInfo(contactInfo: ContactInfo): Observable<ContactInfo> {
    return this.http.put<ContactInfo>(this.API_URL, contactInfo)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to save contact information';
          
          if (error.status === 400) {
            if (error.error && typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error && error.error.message) {
              errorMessage = error.error.message;
            } else if (error.error && typeof error.error === 'object') {
              errorMessage = Object.values(error.error).join(', ');
            }
          } else if (error.status === 404) {
            errorMessage = 'User not found';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }
}
