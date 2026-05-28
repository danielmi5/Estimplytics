import { ErrorHandler, Injectable, inject } from '@angular/core';
import { NotificationService } from './notification.service';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private readonly notificationService = inject(NotificationService);

  handleError(error: unknown): void {
    const message = error instanceof Error ? error.message : 'Error inesperado';

    if (message.includes('Loading chunk') || message.includes('Failed to fetch dynamically imported module')) {
      this.notificationService.error('No se pudo cargar un módulo. Recarga la página.');
      console.error('[ChunkLoadError]', error);
      return;
    }

    console.error('[GlobalErrorHandler]', error);
    this.notificationService.error(message);
  }
}
