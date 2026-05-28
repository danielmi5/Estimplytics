import { HttpInterceptorFn } from '@angular/common/http';
import { tap, catchError, throwError } from 'rxjs';

export const loggingInterceptor: HttpInterceptorFn = (req, next) => {
  const started = Date.now();

  return next(req).pipe(
    tap((event) => {
      if (event.type === 4) {
        const elapsed = Date.now() - started;
        console.info(`[HTTP] ${req.method} ${req.url} - ${elapsed}ms`);
      }
    }),
    catchError((error) => {
      const elapsed = Date.now() - started;
      const status = error.status ?? 'ERR';
      console.error(`[HTTP] ${req.method} ${req.url} - ${status} (${elapsed}ms)`, error);
      return throwError(() => error);
    })
  );
};

