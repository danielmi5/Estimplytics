import { ChangeDetectionStrategy, Component, computed, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import type { ValidationState } from '../form-input/form-input';

@Component({
  selector: 'app-form-textarea',
  standalone: true,
  templateUrl: './form-textarea.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormTextarea),
      multi: true,
    },
  ],
})
export class FormTextarea implements ControlValueAccessor {
  readonly id = input('');
  readonly name = input('');
  readonly label = input('');
  readonly placeholder = input('');
  readonly required = input(false);
  readonly rows = input(4);
  readonly helpText = input<string | undefined>(undefined);
  readonly errorMessage = input<string | undefined>(undefined);
  readonly successMessage = input<string | undefined>(undefined);
  readonly showSuccessMessage = input(false);
  readonly warningMessage = input<string | undefined>(undefined);
  readonly validationState = input<ValidationState>('initial');
  readonly disabled = input(false);

  private readonly controlDisabled = signal(false);
  readonly isDisabled = computed(() => this.disabled() || this.controlDisabled());

  value = '';
  onChange: (value: string) => void = () => {};
  onTouched: () => void = () => {};

  getEffectiveState(): ValidationState {
    return this.validationState();
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

  onInput(event: Event): void {
    const textarea = event.target as HTMLTextAreaElement;
    this.value = textarea.value;
    this.onChange(this.value);
  }

  onBlur(): void {
    this.onTouched();
  }
}
