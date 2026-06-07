import { Component, computed, EventEmitter, inject, Output, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';
import { AppStateService, AuthService, ThemeService } from '../../../services';
import { fromEvent } from 'rxjs';
import { distinctUntilChanged, map } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

interface NavItem {
  label: string;
  path: string;
  ariaLabel: string;
}

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, FeatherIconDirective],
  templateUrl: './header.html'
})
export class Header{
  private readonly document = inject(DOCUMENT);
  protected readonly appState = inject(AppStateService);
  protected readonly theme = inject(ThemeService);

  readonly isScrolled = signal(false);
  readonly mobileMenuOpen = signal(false);

  readonly mobileMenuAriaExpanded = computed(() => String(this.mobileMenuOpen()));
  readonly avatarInitials = computed(() => this.appState.userInitials());
  readonly avatarLabel = computed(() => {
    const user = this.appState.user();
    return user?.name ? `Usuario ${user.name}` : 'Usuario no autenticado';
  });

  @Output() scrolledChange = new EventEmitter<boolean>();

  readonly navItems: NavItem[] = [
    { label: 'Inicio', path: '/', ariaLabel: 'Ir a Inicio' },
    { label: 'Dashboard', path: '/dashboard', ariaLabel: 'Ir a Dashboard' },
    { label: 'Configuración', path: '/configuration', ariaLabel: 'Ir a Configuración' }
  ];

  constructor() {
    inject(AuthService);

    fromEvent(this.document.defaultView!, 'scroll')
      .pipe(
        map(() => this.document.defaultView!.scrollY > 20),
        distinctUntilChanged(),
        takeUntilDestroyed()
      )
      .subscribe((scrolled) => {
        this.isScrolled.set(scrolled);
        this.scrolledChange.emit(scrolled);
      });

    this.emitScrollState();
  }

  private emitScrollState() {
    const scrolled = this.document.defaultView!.scrollY > 20;
    this.isScrolled.set(scrolled);
    this.scrolledChange.emit(scrolled);
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update((open) => !open);
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }

  toggleTheme(): void {
    this.theme.toggle();
  }
}