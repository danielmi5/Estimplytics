import { Injectable, inject, signal } from '@angular/core';
import { EMPTY, catchError, finalize } from 'rxjs';
import { AppStateService } from './app-state.service';
import { UsersApiService } from '../core/users/users-api.service';
import { UserResponse, UserUpdate } from '../core/users/user.dto';
import { NotificationService } from './notification.service';

const USER_KEY = 'auth_user';

@Injectable({ providedIn: 'root' })
export class UserProfileService {
  private readonly appState = inject(AppStateService);
  private readonly usersApi = inject(UsersApiService);
  private readonly notificationService = inject(NotificationService);

  private readonly _isLoading = signal(false);
  readonly isLoading = this._isLoading.asReadonly();

  getStoredUser(): UserResponse | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? (JSON.parse(raw) as UserResponse) : null;
    } catch {
      return null;
    }
  }

  updateProfile(data: UserUpdate): void {
    const user = this.getStoredUser();
    if (!user?.id) {
      this.notificationService.error('No se puede identificar al usuario actual. Vuelve a iniciar sesión.');
      return;
    }

    this._isLoading.set(true);

    this.usersApi
      .update(user.id, data)
      .pipe(
        finalize(() => this._isLoading.set(false)),
        catchError(() => {
          this.notificationService.error('Error al actualizar el perfil. Inténtalo de nuevo.');
          return EMPTY;
        })
      )
      .subscribe((updated) => {
        localStorage.setItem(USER_KEY, JSON.stringify(updated));
        this.appState.setUser(updated);
        this.notificationService.success('Perfil actualizado correctamente.');
      });
  }
}
