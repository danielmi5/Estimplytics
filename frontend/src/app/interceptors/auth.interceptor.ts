import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, firstValueFrom, catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

let refreshPromise: Promise<string> | null = null;

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  const isApi = request.url.startsWith('/') || request.url.startsWith(environment.apiUrl);

  if (token && isApi && !request.url.includes('/api/auth/login') && !request.url.includes('/api/auth/refresh')) {
    const safeToken = token.trim();
    request = request.clone({ setHeaders: { Authorization: `Bearer ${safeToken}` } });
  }

  return next(request).pipe(
    catchError((err: any) => {
      const isAuthEndpoint =
        request.url.includes('/api/auth/') ||
        (request.url.includes('/api/users') && request.method === 'POST');

      if (err?.status === 401 && !isAuthEndpoint) {
        if (!refreshPromise) {
          refreshPromise = firstValueFrom(auth.refreshToken())
            .then((res) => res.accessToken)
            .catch((e) => { refreshPromise = null; throw e; });
        }

        return from(refreshPromise).pipe(
          switchMap((newToken) => {
            refreshPromise = null;
            const newRequest = request.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } });
            return next(newRequest);
          }),
          catchError((e) => {
            auth.logout();
            return throwError(() => e);
          })
        );
      }

      return throwError(() => err);
    })
  );
};
