import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';

const THEME_STORAGE_KEY = 'estimplytics-theme';

function readStoredThemePreference(): boolean | null {
  if (typeof localStorage === 'undefined') {
    return null;
  }

  const stored = localStorage.getItem(THEME_STORAGE_KEY);

  if (stored === 'dark') {
    return true;
  }

  if (stored === 'light') {
    return false;
  }

  return null;
}

function readSystemPrefersDark(): boolean {
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  return false;
}

function resolveInitialDark(): boolean {
  const stored = readStoredThemePreference();

  if (stored !== null) {
    return stored;
  }

  return readSystemPrefersDark();
}

function applyThemeToDocument(documentRef: Document, isDark: boolean): void {
  documentRef.documentElement.classList.toggle('dark', isDark);
}

export function initTheme(documentRef: Document = document): void {
  applyThemeToDocument(documentRef, resolveInitialDark());
}

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);

  readonly isDark = signal(resolveInitialDark());

  constructor() {
    this.applyTheme(this.isDark());
  }

  toggle(): void {
    this.isDark.update((current) => !current);
    this.persistAndApply(this.isDark());
  }

  setDark(enabled: boolean): void {
    this.isDark.set(enabled);
    this.persistAndApply(enabled);
  }

  private persistAndApply(isDark: boolean): void {
    this.applyTheme(isDark);

    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(THEME_STORAGE_KEY, isDark ? 'dark' : 'light');
    }
  }

  private applyTheme(isDark: boolean): void {
    applyThemeToDocument(this.document, isDark);
  }
}
