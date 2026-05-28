import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AppStateService, AppUser } from './app-state.service';
import { LoginRequest, RegisterRequest, TokenResponse } from '../models/auth.models';
import { UserResponse } from '../core/users/user.dto';

const ACCESS_TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_KEY = 'auth_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly appState = inject(AppStateService);
  private readonly router = inject(Router);
  private readonly apiUrl = environment.apiUrl;

  constructor() {
    this.restoreSession();
  }

  login(credentials: LoginRequest): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.apiUrl}/auth/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
        localStorage.setItem(USER_KEY, JSON.stringify(response.user));
        this.appState.setUser({ email: response.user.email, name: response.user.name, role: response.user.role });
      })
    );
  }

  register(data: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.apiUrl}/users`, {
      name: data.name,
      email: data.email,
      password: data.password,
      role: 'ANALYST'
    });
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.appState.setUser(null);
    void this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  private restoreSession(): void {
    const access = this.getToken();
    const user = localStorage.getItem(USER_KEY);
    if (!access) {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
      localStorage.removeItem(REFRESH_TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      return;
    }
    if (user) {
      try {
        const u = JSON.parse(user) as UserResponse;
        this.appState.setUser({ email: u.email, name: u.name, role: u.role });
      } catch {
        this.appState.setUser(null);
      }
    }
  }

  refreshToken(): Observable<TokenResponse> {
    const refresh = this.getRefreshToken();
    if (!refresh) return throwError(() => new Error('No refresh token'));

    return this.http.post<TokenResponse>(`${this.apiUrl}/auth/refresh`, { refreshToken: refresh }).pipe(
      tap((response) => {
        localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
        if (response.user) {
          localStorage.setItem(USER_KEY, JSON.stringify(response.user));
          this.appState.setUser({ email: response.user.email, name: response.user.name, role: response.user.role });
        }
      })
    );
  }
}
