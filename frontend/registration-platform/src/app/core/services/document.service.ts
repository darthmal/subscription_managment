import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Document {
  id: number;
  documentType: string;
  originalFilename: string;
  fileSize: number;
  contentType: string;
  status: 'UPLOADED' | 'VALIDATED' | 'REJECTED';
  uploadedAt: string;
  validatedAt: string | null;
  validationNotes: string | null;
}

export enum DocumentType {
  ID_PHOTO = 'ID_PHOTO',
  ID_CARD_FRONT = 'ID_CARD_FRONT',
  ID_CARD_BACK = 'ID_CARD_BACK',
  PASSPORT = 'PASSPORT',
  DIPLOMA_BAC = 'DIPLOMA_BAC',
  TRANSCRIPT = 'TRANSCRIPT',
  MOTIVATION_LETTER = 'MOTIVATION_LETTER',
  RECOMMENDATION_LETTER = 'RECOMMENDATION_LETTER',
  OTHER = 'OTHER'
}

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly API_URL = `${environment.apiUrl}/api/applicant/documents`;

  constructor(private http: HttpClient) {}

  uploadDocument(file: File, documentType: DocumentType): Observable<Document> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<Document>(`${this.API_URL}/${documentType}`, formData)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to upload document';
          
          if (error.status === 400) {
            if (error.error && typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error && typeof error.error === 'object') {
              errorMessage = Object.values(error.error).join(', ');
            }
          } else if (error.status === 404) {
            errorMessage = 'Invalid document type';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  getDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(this.API_URL)
      .pipe(
        catchError(error => {
          const errorMessage = 'Failed to retrieve documents';
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  downloadDocument(id: number): Observable<HttpEvent<Blob>> {
    const request = new HttpRequest('GET', `${this.API_URL}/${id}/download`, {
      responseType: 'blob',
      reportProgress: true
    });

    return this.http.request<Blob>(request)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to download document';
          
          if (error.status === 404) {
            errorMessage = 'Document not found';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  deleteDocument(id: number): Observable<string> {
    return this.http.delete<string>(`${this.API_URL}/${id}`)
      .pipe(
        catchError(error => {
          let errorMessage = 'Failed to delete document';
          
          if (error.status === 404) {
            errorMessage = 'Document not found';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }
}
