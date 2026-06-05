import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormInput } from '../../shared/form-input/form-input';
import { FormSelect, FormSelectOption } from '../../shared/form-select/form-select';
import { FormTextarea } from '../../shared/form-textarea/form-textarea';
import { RequestsStateService } from '../../../core/requests/requests-state.service';
import { RequestRequest } from '../../../core/requests/request.dto';
import { getRequestFieldMessage, getRequestFieldState, type RequestFieldName, type RequestFieldState } from '../../../form/validators/request.helpers';

@Component({
  selector: 'app-request-form',
  standalone: true,
  imports: [ReactiveFormsModule, FormInput, FormSelect, FormTextarea],
  templateUrl: './request-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RequestForm {
  private readonly fb = inject(FormBuilder);
  readonly state = inject(RequestsStateService);

  readonly priorityOptions: FormSelectOption[] = [
    { value: 'High', label: 'Alta' },
    { value: 'Normal', label: 'Normal' },
    { value: 'Low', label: 'Baja' },
  ];

  readonly form = this.fb.group({
    project: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(200)]],
    description: ['', Validators.maxLength(2000)],
    demandType: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    priority: ['Normal', Validators.required],
    status: ['OPEN', Validators.required],
  });

  fieldState(fieldName: RequestFieldName): RequestFieldState {
    return getRequestFieldState(this.form.get(fieldName));
  }

  fieldMessage(fieldName: RequestFieldName, state: RequestFieldState): string {
    return getRequestFieldMessage(fieldName, this.form.get(fieldName), state);
  }

  submit(): void {
    this.form.markAllAsTouched();

    if (this.form.invalid || this.state.isLoading()) {
      return;
    }

    const raw = this.form.getRawValue();
    const body: RequestRequest = {
      projectName: (raw.project ?? '').trim(),
      title: (raw.title ?? '').trim(),
      description: (raw.description ?? '').trim() || undefined,
      demandType: (raw.demandType ?? '').trim(),
      priority: raw.priority ?? 'Normal',
      status: raw.status ?? 'OPEN',
    };

    this.state.create(body);
  }

  resetForm(): void {
    this.form.reset({
      project: '',
      title: '',
      description: '',
      demandType: '',
      priority: 'Normal',
      status: 'OPEN',
    });
  }
}
