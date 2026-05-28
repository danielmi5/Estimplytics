import { Component, ChangeDetectionStrategy, computed, input, output } from '@angular/core';
import { Params, RouterLink } from '@angular/router';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [RouterLink, FeatherIconDirective],
  templateUrl: './button.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Button {
  readonly variant = input.required<'primary' | 'secondary' | 'ghost'>();
  readonly label = input.required<string>();
  readonly size = input<'sm' | 'md' | 'lg'>('md');
  readonly buttonType = input<'button' | 'submit' | 'reset'>('button');
  readonly fullWidth = input(false);
  readonly routerLink = input<string | string[] | null>(null);
  readonly queryParams = input<Params | null>(null);
  readonly ariaLabel = input<string | null>(null);
  readonly icon = input<string | null>(null);
  readonly disabled = input(false);

  readonly pressed = output<MouseEvent>();

  readonly buttonClasses = computed(() => {
    const classes = [`button`, `button--${this.variant()}`, `button--${this.size()}`];

    if (this.fullWidth()) {
      classes.push('button--full-width');
    }

    return classes.join(' ');
  });

  handleClick(event: MouseEvent): void {
    this.pressed.emit(event);
  }
}
