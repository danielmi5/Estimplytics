import { HttpErrorResponse } from '@angular/common/http';

interface ApiErrorBody {
  message?: string;
  description?: string;
}

export function getApiErrorMessage(err: unknown, fallback: string): string {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as ApiErrorBody | null;

    if (body?.message) {
      return body.message;
    }

    if (err.status === 403) {
      return 'No tienes permiso para realizar esta operación.';
    }

    if (err.status === 409) {
      return body?.description ?? 'La operación entra en conflicto con datos existentes.';
    }

    return fallback;
  }

  if (err instanceof Error) {
    return err.message;
  }

  return fallback;
}
