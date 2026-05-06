import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const requiredRole = route.data['role'] as string;
  const userRole = authService.getRole();

  if (userRole === requiredRole) {
    return true;
  }

  if (userRole) {
    authService.navigateByRole(userRole);
  } else {
    router.navigate(['/']);
  }
  return false;
};
