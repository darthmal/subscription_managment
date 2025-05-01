import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UserSummaryDTO {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  provider: string;
  enabled: boolean;
  locked: boolean;
  createdAt: string;
  applicationStatus?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface PersonalInfoDTO {
  lastName: string;
  firstNames: string;
  gender: string;
  dateOfBirth: string;
  nationality: string;
  idDocumentType: string;
}

export interface AddressDTO {
  street: string;
  street2?: string;
  city: string;
  postalCode: string;
  country: string;
  latitude?: number;
  longitude?: number;
}

export interface EmergencyContactDTO {
  name: string;
  relationship: string;
  phone: string;
}

export interface ContactInfoDTO {
  emailVerified: boolean;
  phoneNumber: string;
  address: AddressDTO;
  emergencyContact: EmergencyContactDTO;
}

export interface AcademicHistoryDTO {
  id: number;
  institutionName: string;
  specialization: string;
  startDate: string;
  endDate: string;
}

export interface DocumentDTO {
  id: number;
  type: string;
  documentType: string;
  fileName: string;
  originalFilename: string;
  fileSize: number;
  contentType: string;
  status: string;
  uploadedAt: string;
  validatedAt?: string;
  validationNotes?: string;
}

export interface ApplicationDetailDTO {
  userSummary: UserSummaryDTO;
  personalInfo: PersonalInfoDTO;
  contactInfo: ContactInfoDTO;
  academicHistory: AcademicHistoryDTO[];
  documents: DocumentDTO[];
}

export interface DocumentStatusUpdateDTO {
  newStatus: 'APPROVED' | 'VALIDATED' | 'REJECTED' | 'PENDING_REVIEW';
  validationNotes?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly API_URL = `${environment.apiUrl}/api/admin`;

  constructor(private http: HttpClient) {}

  // User Management
  getUsers(page: number = 0, size: number = 20, sort?: string): Observable<PageResponse<UserSummaryDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (sort) {
      params = params.append('sort', sort);
    }
    
    return this.http.get<PageResponse<UserSummaryDTO>>(`${this.API_URL}/users`, { params });
  }

  getUserApplication(userId: number): Observable<ApplicationDetailDTO> {
    return this.http.get<ApplicationDetailDTO>(`${this.API_URL}/users/${userId}/application`);
  }

  // Document Management
  updateDocumentStatus(documentId: number, statusUpdate: DocumentStatusUpdateDTO): Observable<DocumentDTO> {
    return this.http.put<DocumentDTO>(`${this.API_URL}/users/documents/${documentId}/status`, statusUpdate);
  }

  // Application Status Management
  updateApplicationStatus(userId: number, newStatus: string): Observable<UserSummaryDTO> {
    return this.http.put<UserSummaryDTO>(`${this.API_URL}/users/${userId}/status`, JSON.stringify(newStatus), {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  // Dashboard Statistics
  getCompletionRate(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/users/dashboard/completion-rate`);
  }

  getTotalApplicationsCount(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/users/dashboard/total-applications`);
  }

  getPendingApplicationsCount(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/users/dashboard/pending-count`);
  }

  getRejectedApplicationsCount(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/users/dashboard/rejected-count`);
  }

  getApprovedApplicationsCount(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/users/dashboard/approved-count`);
  }
}
