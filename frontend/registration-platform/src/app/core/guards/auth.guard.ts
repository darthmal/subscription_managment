import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.getToken()) {
    // Check if token is expired
    const userData = authService.getUserData();
    if (!userData || userData.tokenExpiry < Date.now()) {
      // Token expired, redirect to login
      authService.logout();
      router.navigate(['/auth/login'], { 
        queryParams: { expired: 'true' } 
      });
      return false;
    }
    return true;
  }
  
  // Not authenticated, redirect to login
  router.navigate(['/auth/login']);
  return false;
};

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.getToken() && authService.isAdmin()) {
    return true;
  }
  
  // Not an admin, redirect to home or appropriate page
  if (authService.getToken()) {
    // User is authenticated but not an admin
    router.navigate(['/']);
    return false;
  }
  
  // Not authenticated, redirect to login
  router.navigate(['/auth/login']);
  return false;
};

export const applicantGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.getToken() && authService.isApplicant()) {
    return true;
  }
  
  // Not an applicant, redirect to home or appropriate page
  if (authService.getToken()) {
    // User is authenticated but not an applicant
    router.navigate(['/']);
    return false;
  }
  
  // Not authenticated, redirect to login
  router.navigate(['/auth/login']);
  return false;
};
