import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';
import { FormInput } from '../../shared/form-input/form-input';
import { FormSelect, FormSelectOption } from '../../shared/form-select/form-select';
import { FormTextarea } from '../../shared/form-textarea/form-textarea';
import { Button } from '../../shared/button/button';
import type { StepState } from '../../../pages/analysis/step-state';
import type { ImpactAnalysisDocumentData } from '../analysis.models';

type AnalysisFieldName =
  | 'descripcionAbreviada'
  | 'descripcionImpacto'
  | 'descripcionSolucion'
  | 'requisitosFuncionales'
  | 'pruebas'
  | 'complexity'
  | 'versionNumber';

type AnalysisFieldState = 'initial' | 'warning' | 'error' | 'success';

@Component({
  selector: 'app-step-analysis',
  imports: [ReactiveFormsModule, FormInput, FormSelect, FormTextarea, Button, FeatherIconDirective],
  templateUrl: './step-analysis.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepAnalysis {
  private readonly fb = inject(FormBuilder);

  readonly state = input<StepState>('inactive');
  readonly summary = input<ImpactAnalysisDocumentData | null>(null);
  readonly isSubmitting = input(false);
  readonly submitError = input<string | null>(null);

  readonly stepCompleted = output<ImpactAnalysisDocumentData>();
  readonly stepEdit = output<void>();

  readonly completedSummary = computed(() => {
    const data = this.summary();
    return data?.descripcionAbreviada?.trim() ?? '';
  });

  readonly complexityOptions: FormSelectOption[] = [
    { value: 'LOW', label: 'Baja' },
    { value: 'MEDIUM', label: 'Media' },
    { value: 'HIGH', label: 'Alta' },
  ];

  readonly form = this.fb.group({
    descripcionAbreviada: ['', [Validators.required, Validators.maxLength(500)]],
    descripcionImpacto: ['', [Validators.required, Validators.maxLength(2000)]],
    descripcionSolucion: ['', [Validators.required, Validators.maxLength(2000)]],
    requisitosFuncionales: ['', [Validators.required, Validators.maxLength(2000)]],
    pruebas: ['', [Validators.required, Validators.maxLength(2000)]],
    complexity: ['MEDIUM', Validators.required],
    versionNumber: [1, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    effect(() => {
      const data = this.summary();
      if (data) {
        this.form.patchValue(
          {
            descripcionAbreviada: data.descripcionAbreviada,
            descripcionImpacto: data.descripcionImpacto,
            descripcionSolucion: data.descripcionSolucion,
            requisitosFuncionales: data.requisitosFuncionales,
            pruebas: data.pruebas,
            complexity: data.complexity,
            versionNumber: data.versionNumber,
          },
          { emitEvent: false }
        );
      }
    });
  }

  fieldState(fieldName: AnalysisFieldName): AnalysisFieldState {
    const control = this.form.get(fieldName);
    if (!control || (!control.touched && !control.dirty)) {
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

  fieldMessage(fieldName: AnalysisFieldName, kind: 'error' | 'warning' | 'success'): string {
    const control = this.form.get(fieldName);
    if (!control) {
      return '';
    }

    const labels: Record<AnalysisFieldName, string> = {
      descripcionAbreviada: 'Descripción abreviada',
      descripcionImpacto: 'Descripción del impacto',
      descripcionSolucion: 'Descripción de la solución',
      requisitosFuncionales: 'Requisitos funcionales',
      pruebas: 'Pruebas',
      complexity: 'Complejidad',
      versionNumber: 'Versión',
    };

    if (kind === 'warning') {
      return `${labels[fieldName]} es obligatorio`;
    }
    if (kind === 'success') {
      return `${labels[fieldName]} correcto`;
    }
    if (control.errors?.['maxlength']) {
      return `${labels[fieldName]} supera la longitud máxima`;
    }
    if (control.errors?.['min']) {
      return `${labels[fieldName]} debe ser al menos 1`;
    }
    return `${labels[fieldName]} no es válido`;
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.isSubmitting()) {
      return;
    }

    const raw = this.form.getRawValue();
    this.stepCompleted.emit({
      descripcionAbreviada: (raw.descripcionAbreviada ?? '').trim(),
      descripcionImpacto: (raw.descripcionImpacto ?? '').trim(),
      descripcionSolucion: (raw.descripcionSolucion ?? '').trim(),
      requisitosFuncionales: (raw.requisitosFuncionales ?? '').trim(),
      pruebas: (raw.pruebas ?? '').trim(),
      complexity: raw.complexity ?? 'MEDIUM',
      versionNumber: Number(raw.versionNumber ?? 1),
    });
  }

  onEdit(): void {
    this.stepEdit.emit();
  }
}
