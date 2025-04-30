import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  email: string;
  roles: string[];
}

export interface UserData {
  userId: number;
  email: string;
  roles: string[];
  tokenExpiry: number; // Unix timestamp for token expiration
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/api/auth`;
  private readonly TOKEN_EXPIRY_TIME = 60 * 60 * 1000; // 1 hour in milliseconds
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  private userDataSubject = new BehaviorSubject<UserData | null>(this.getUserData());
  
  isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  userData$ = this.userDataSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, request)
      .pipe(
        tap(response => {
          this.storeAuthData(response);
        }),
        catchError(error => {
          let errorMessage = 'Registration failed';
          
          if (error.error === 'Error: Email address is already taken!') {
            errorMessage = 'Email address is already taken';
          } else if (error.error && typeof error.error === 'object') {
            // Handle validation errors
            errorMessage = Object.values(error.error).join(', ');
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, request)
      .pipe(
        tap(response => {
          this.storeAuthData(response);
        }),
        catchError(error => {
          let errorMessage = 'Login failed';
          
          if (error.status === 401) {
            errorMessage = 'Invalid email or password';
          }
          
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userData');
    this.isAuthenticatedSubject.next(false);
    this.userDataSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getUserData(): UserData | null {
    const userDataStr = localStorage.getItem('userData');
    if (!userDataStr) return null;
    
    try {
      const userData = JSON.parse(userDataStr) as UserData;
      
      // Check if token has expired
      if (userData.tokenExpiry < Date.now()) {
        this.logout();
        return null;
      }
      
      return userData;
    } catch (e) {
      this.logout();
      return null;
    }
  }

  isAdmin(): boolean {
    const userData = this.getUserData();
    return !!userData?.roles.includes('ROLE_ADMIN');
  }

  isApplicant(): boolean {
    const userData = this.getUserData();
    return !!userData?.roles.includes('ROLE_APPLICANT');
  }

  private hasToken(): boolean {
    return !!this.getToken() && !!this.getUserData();
  }

  private storeAuthData(response: AuthResponse): void {
    // Store token
    localStorage.setItem('accessToken', response.accessToken);
    
    // Store user data with expiry time
    const userData: UserData = {
      userId: response.userId,
      email: response.email,
      roles: response.roles,
      tokenExpiry: Date.now() + this.TOKEN_EXPIRY_TIME
    };
    
    localStorage.setItem('userData', JSON.stringify(userData));
    
    // Update subjects
    this.isAuthenticatedSubject.next(true);
    this.userDataSubject.next(userData);
  }
}
