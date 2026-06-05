import { ChangeDetectionStrategy, Component, computed, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';
import type { ValidationState } from '../form-input/form-input';

export interface FormSelectOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-form-select',
  standalone: true,
  imports: [FeatherIconDirective],
  templateUrl: './form-select.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormSelect),
      multi: true,
    },
  ],
})
export class FormSelect implements ControlValueAccessor {
  readonly id = input('');
  readonly name = input('');
  readonly label = input('');
  readonly placeholder = input('');
  readonly required = input(false);
  readonly helpText = input<string | undefined>(undefined);
  readonly errorMessage = input<string | undefined>(undefined);
  readonly successMessage = input<string | undefined>(undefined);
  readonly showSuccessMessage = input(false);
  readonly warningMessage = input<string | undefined>(undefined);
  readonly validationState = input<ValidationState>('initial');
  readonly disabled = input(false);
  readonly options = input<FormSelectOption[]>([]);

  private readonly controlDisabled = signal(false);
  readonly isDisabled = computed(() => this.disabled() || this.controlDisabled());

  value = '';
  onChange: (value: string) => void = () => {};
  onTouched: () => void = () => {};

  getEffectiveState(): ValidationState {
    return this.validationState();
  }

  getRightIcon(): string | null {
    const state = this.getEffectiveState();
    if (state === 'success') return 'check';
    if (state === 'error') return 'alert-circle';
    if (state === 'warning') return 'alert-triangle';
    return 'chevron-down';
  }

  writeValue(value: string): void {
    this.value = value ?? '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.controlDisabled.set(isDisabled);
  }

  onSelect(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.value = select.value;
    this.onChange(this.value);
  }

  onBlur(): void {
    this.onTouched();
  }
}
