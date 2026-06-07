import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';

@Component({
  selector: 'app-sidebar-nav-item',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, FeatherIconDirective],
  templateUrl: './sidebar-nav-item.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidebarNavItem {
  readonly label = input.required<string>();
  readonly icon = input.required<string>();
  readonly route = input<string | null>(null);
  readonly exact = input(false);

  readonly itemClick = output<void>();

  handleClick(): void {
    this.itemClick.emit();
  }
}
