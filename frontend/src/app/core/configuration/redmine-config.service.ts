import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { EMPTY, Observable, catchError, finalize, of, tap } from 'rxjs';
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
  private readonly _isSyncing = signal(false);
  private readonly _credentialsChecked = signal(false);

  readonly credentials = this._credentials.asReadonly();
  readonly status = this._status.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly isSyncing = this._isSyncing.asReadonly();
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

  ensureCredentialsLoaded(): void {
    if (this._credentialsChecked()) {
      return;
    }

    this.refreshCredentials().subscribe();
  }

  refreshCredentials(): Observable<RedmineCredentialResponse[]> {
    return this.http.get<RedmineCredentialResponse[]>(this.baseUrl).pipe(
      catchError(() => of([])),
      tap((credentials) => {
        this._credentials.set(credentials);
        this._status.set(credentials.length > 0 ? 'connected' : 'none');
        this._credentialsChecked.set(true);
      })
    );
  }

  loadCredentials(): void {
    this._credentialsChecked.set(true);
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

  syncIssues(credentialId: number, fullSync = false, silent = false): Observable<string> {
    this._isSyncing.set(true);

    const params = new HttpParams()
      .set('credentialId', credentialId)
      .set('fullSync', fullSync);

    return this.http
      .post(`${environment.apiUrl}/redmine/sync`, null, { params, responseType: 'text' })
      .pipe(
        tap((message) => {
          if (!silent) {
            this.notificationService.success(message || 'Peticiones sincronizadas correctamente.');
          }
        }),
        catchError(() => {
          if (!silent) {
            this.notificationService.error('No se pudieron sincronizar las peticiones. Inténtalo de nuevo.');
          }
          return EMPTY;
        }),
        finalize(() => this._isSyncing.set(false))
      );
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

  deleteCredential(credentialId: number): void {
    this._isLoading.set(true);

    this.http
      .delete<void>(`${this.baseUrl}/${credentialId}`)
      .pipe(
        finalize(() => this._isLoading.set(false)),
        catchError(() => {
          this.notificationService.error('No se pudieron eliminar las credenciales de Redmine.');
          return EMPTY;
        })
      )
      .subscribe(() => {
        this._credentials.set([]);
        this._status.set('none');
        this.notificationService.info('Credenciales de Redmine eliminadas.');
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
