import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AcademicHistory {
  id?: number;
  institutionName: string;
  specialization: string;
  startDate: string;
  endDate?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AcademicHistoryService {
  private readonly API_URL = `${environment.apiUrl}/api/applicant/academic-history`;

  constructor(private http: HttpClient) {}

  getAcademicHistory(): Observable<AcademicHistory[]> {
    return this.http.get<AcademicHistory[]>(this.API_URL)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to retrieve academic history';
          
          if (error.status === 404) {
            errorMessage = 'No academic history found';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  addAcademicHistory(academicHistory: AcademicHistory): Observable<AcademicHistory> {
    return this.http.post<AcademicHistory>(this.API_URL, academicHistory)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to add academic history';
          
          if (error.status === 400) {
            if (error.error && typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error && error.error.message) {
              errorMessage = error.error.message;
            } else if (error.error && typeof error.error === 'object') {
              errorMessage = Object.values(error.error).join(', ');
            }
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  updateAcademicHistory(id: number, academicHistory: AcademicHistory): Observable<AcademicHistory> {
    return this.http.put<AcademicHistory>(`${this.API_URL}/${id}`, academicHistory)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to update academic history';
          
          if (error.status === 400) {
            if (error.error && typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error && error.error.message) {
              errorMessage = error.error.message;
            } else if (error.error && typeof error.error === 'object') {
              errorMessage = Object.values(error.error).join(', ');
            }
          } else if (error.status === 404) {
            errorMessage = 'Academic history not found';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  deleteAcademicHistory(id: number): Observable<string> {
    return this.http.delete<string>(`${this.API_URL}/${id}`)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to delete academic history';
          
          if (error.status === 404) {
            errorMessage = 'Academic history not found';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }
}
