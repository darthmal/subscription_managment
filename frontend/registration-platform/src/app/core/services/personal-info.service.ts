import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PersonalInfo {
  lastName: string;
  firstNames: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  dateOfBirth: string; // ISO format: YYYY-MM-DD
  nationality: string;
  idDocumentType: 'NATIONAL_ID_CARD' | 'PASSPORT' | 'DRIVERS_LICENSE' | 'OTHER';
}

@Injectable({
  providedIn: 'root'
})
export class PersonalInfoService {
  private readonly API_URL = `${environment.apiUrl}/api/applicant/personal-info`;

  constructor(private http: HttpClient) {}

  getPersonalInfo(): Observable<PersonalInfo> {
    return this.http.get<PersonalInfo>(this.API_URL)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to retrieve personal information';
          
          if (error.status === 404) {
            errorMessage = 'No personal information found';
          } else if (error.status === 403) {
            errorMessage = 'You do not have permission to access this resource';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  savePersonalInfo(personalInfo: PersonalInfo): Observable<PersonalInfo> {
    return this.http.put<PersonalInfo>(this.API_URL, personalInfo)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to save personal information';
          
          if (error.status === 400) {
            if (error.error === 'Applicant must be at least 16 years old.') {
              errorMessage = 'Applicant must be at least 16 years old';
            } else if (error.error && typeof error.error === 'object') {
              // Handle validation errors
              errorMessage = Object.values(error.error).join(', ');
            }
          } else if (error.status === 403) {
            errorMessage = 'You do not have permission to access this resource';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }
}
