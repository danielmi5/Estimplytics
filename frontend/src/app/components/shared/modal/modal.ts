import { ChangeDetectionStrategy, Component, HostListener, effect, input, output } from '@angular/core';
import { Button } from '../button/button';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [Button, FeatherIconDirective],
  templateUrl: './modal.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Modal {
  readonly titleId = 'modal-title';

  readonly open = input(false);
  readonly title = input.required<string>();
  readonly secondaryLabel = input('Cancelar');
  readonly primaryLabel = input.required<string>();
  readonly disabled = input(false);

  readonly closed = output<void>();
  readonly secondaryAction = output<void>();
  readonly primaryAction = output<void>();

  private previouslyFocused: HTMLElement | null = null;

  constructor() {
    effect(() => {
      if (this.open()) {
        this.previouslyFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null;
        document.body.style.overflow = 'hidden';
        return;
      }

      document.body.style.overflow = '';
      this.previouslyFocused?.focus();
      this.previouslyFocused = null;
    });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.open()) {
      this.close();
    }
  }

  close(): void {
    this.closed.emit();
  }
}
