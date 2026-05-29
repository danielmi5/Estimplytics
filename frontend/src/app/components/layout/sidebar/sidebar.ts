import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AppStateService } from '../../../services/app-state.service';
import { AuthService } from '../../../services/auth.service';
import { SidebarNavItem } from '../../shared/sidebar-nav-item/sidebar-nav-item';

interface NavItem {
  label: string;
  route: string;
  icon: string;
  exact: boolean;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [SidebarNavItem],
  templateUrl: './sidebar.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Sidebar {
  private readonly appState = inject(AppStateService);
  private readonly auth = inject(AuthService);

  readonly user = this.appState.user;
  readonly userInitials = this.appState.userInitials;

  readonly navItems: NavItem[] = [
    { label: 'Dashboard', route: '/', icon: 'home', exact: true  },
    { label: 'Peticiones', route: '/requests', icon: 'pie-chart', exact: false },
    { label: 'Análisis', route: '/analytics', icon: 'slack', exact: false },
    { label: 'Historial', route: '/history', icon: 'layers', exact: false },
    { label: 'Configuración', route: '/configuration', icon: 'settings', exact: false },
];

  logout(): void {
    this.auth.logout();
  }
}
