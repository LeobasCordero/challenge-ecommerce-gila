import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStateService } from '../services/auth-state.service';

/**
 * Route guard enforcing authentication sessions and user role authorizations.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authState = inject(AuthStateService);
  const router = inject(Router);

  if (authState.isAuthenticated()) {
    if (state.url.startsWith('/admin') && !authState.isAdmin()) {
      router.navigate(['/catalog']);
      return false;
    }
    return true;
  }

  router.navigate(['/login']);
  return false;
};
