import { AbstractControl } from '@angular/forms';

export type RequestFieldName = 'project' | 'title' | 'description' | 'demandType' | 'priority';
export type RequestFieldState = 'initial' | 'warning' | 'error' | 'success';

const messages = {
  warning: {
    project: 'El proyecto es obligatorio',
    title: 'El título es obligatorio',
    demandType: 'El tipo de demanda es obligatorio',
    priority: 'La prioridad es obligatoria',
  },
  error: {
    project: 'El nombre del proyecto debe tener al menos 2 caracteres',
    title: 'El título debe tener al menos 3 caracteres',
    description: 'La descripción no puede superar los 2000 caracteres',
    demandType: 'El tipo de demanda debe tener al menos 3 caracteres',
    priority: 'Selecciona una prioridad válida',
  },
  success: {
    project: 'Nombre de proyecto correcto',
    title: 'El título es correcto',
    description: 'La descripción es correcta',
    demandType: 'El tipo de demanda es correcto',
    priority: 'Prioridad seleccionada',
  },
} as const;

const touchedOrDirty = (control: AbstractControl, force = false): boolean =>
  force || control.touched || control.dirty;

export function getRequestFieldState(control: AbstractControl | null, force = false): RequestFieldState {
  if (!control || !touchedOrDirty(control, force)) {
    return 'initial';
  }

  if (control.pending) {
    return 'initial';
  }

  if (control.valid) {
    return 'success';
  }

  if (control.errors?.['required'] && !control.value) {
    return 'warning';
  }

  return 'error';
}

export function getRequestFieldMessage(
  fieldName: RequestFieldName,
  control: AbstractControl | null,
  state: RequestFieldState
): string {
  if (!control) {
    return '';
  }

  if (state === 'warning') {
    return messages.warning[fieldName as keyof typeof messages.warning] ?? '';
  }

  if (state === 'success') {
    return messages.success[fieldName] ?? '';
  }

  if ((fieldName === 'project' || fieldName === 'title' || fieldName === 'demandType') && control.errors?.['minlength']) {
    return messages.error[fieldName];
  }

  if (fieldName === 'description' && control.errors?.['maxlength']) {
    return messages.error.description;
  }

  if (control.errors?.['required']) {
    return messages.warning[fieldName as keyof typeof messages.warning] ?? '';
  }

  return messages.error[fieldName as keyof typeof messages.error] ?? '';
}
