import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStateService } from '../state/auth-state.service';
import { BASE_PATH } from '../api/variables';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authState = inject(AuthStateService);
  const token = authState.token();
  const basePath = inject(BASE_PATH, { optional: true });

  // Only attach header if outgoing request targets our backend APIs
  const isApiRequest = !basePath || req.url.startsWith(basePath) || req.url.includes('/api/v1/');

  if (token && isApiRequest) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
