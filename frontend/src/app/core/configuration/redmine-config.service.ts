import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EMPTY, catchError, finalize, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ConnectionStatus, RedmineCredentialRequest, RedmineCredentialResponse } from './redmine-config.dto';
import { NotificationService } from '../../services/notification.service';

@Injectable({ providedIn: 'root' })
export class RedmineConfigService {
  private readonly http = inject(HttpClient);
  private readonly notificationService = inject(NotificationService);
  private readonly baseUrl = `${environment.apiUrl}/v1/redmine-credentials`;

  private readonly _credentials = signal<RedmineCredentialResponse[]>([]);
  private readonly _status = signal<ConnectionStatus>('none');
  private readonly _isLoading = signal(false);

  readonly credentials = this._credentials.asReadonly();
  readonly status = this._status.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly isConnected = computed(() => this._status() === 'connected');
  readonly primaryCredential = computed(() => this._credentials()[0] ?? null);

  readonly statusLabel = computed(() => {
    switch (this._status()) {
      case 'connected': return 'Conectado';
      case 'loading':   return 'Verificando…';
      case 'error':     return 'Error';
      default:          return 'Desconectado';
    }
  });

  loadCredentials(): void {
    this._isLoading.set(true);
    this._status.set('loading');

    this.http
      .get<RedmineCredentialResponse[]>(this.baseUrl)
      .pipe(
        finalize(() => this._isLoading.set(false)),
        catchError(() => {
          this.notificationService.error('No se pudieron cargar las credenciales de Redmine.');
          this._status.set('error');
          return of([]);
        })
      )
      .subscribe((credentials) => {
        this._credentials.set(credentials);
        const first = credentials[0];
        if (first) {
          this.testConnection(first.id);
        } else {
          this._status.set('none');
        }
      });
  }

  save(request: RedmineCredentialRequest): void {
    this._isLoading.set(true);

    this.http
      .post<void>(this.baseUrl, request)
      .pipe(
        finalize(() => this._isLoading.set(false)),
        catchError(() => {
          this.notificationService.error('Error al guardar las credenciales. Inténtalo de nuevo.');
          return EMPTY;
        })
      )
      .subscribe(() => {
        this.notificationService.info('Credenciales guardadas. Verificando conexión…');
        this.loadCredentials();
      });
  }

  private testConnection(credentialId: number): void {
    this._status.set('loading');

    this.http
      .get<{ status: string }>(`${this.baseUrl}/${credentialId}/test`)
      .subscribe({
        next: ({ status }) => {
          switch (status) {
            case 'connected':
              this._status.set('connected');
              this.notificationService.success('Conectado correctamente a Redmine.');
              break;
            case 'unauthorized':
              this._status.set('error');
              this.notificationService.error('Redmine requiere autenticación. Proporciona tu API Key.');
              break;
            default:
              this._status.set('error');
              this.notificationService.error('La URL de Redmine no es accesible. Verifica que sea correcta.');
          }
        },
        error: () => {
          this._status.set('error');
          this.notificationService.error('No se pudo verificar la conexión con Redmine.');
        },
      });
  }
}
