import { ChangeDetectionStrategy, Component, computed, effect, input, output, signal } from '@angular/core';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';
import { Button } from '../../shared/button/button';
import type { StepState } from '../../../pages/analysis/step-state';
import {
  HOUR_TYPE_LABELS,
  HOUR_TYPES,
  sumHourBreakdown,
  type EstimationViewModel,
  type HourBreakdown,
  type HourType,
} from '../analysis.models';

@Component({
  selector: 'app-step-estimation',
  imports: [Button, FeatherIconDirective],
  templateUrl: './step-estimation.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepEstimation {
  readonly state = input<StepState>('inactive');
  readonly estimation = input<EstimationViewModel | null>(null);
  readonly approvedBreakdown = input<HourBreakdown | null>(null);
  readonly isLoading = input(false);
  readonly isValidating = input(false);
  readonly errorMessage = input<string | null>(null);

  readonly stepCompleted = output<HourBreakdown>();
  readonly stepEdit = output<void>();

  readonly completedSummary = computed(() => {
    const breakdown = this.approvedBreakdown();
    if (!breakdown) {
      return '';
    }

    const total = breakdown.hoursPlanning + breakdown.hoursAnalysis + breakdown.hoursDevelopment + breakdown.hoursTesting;
    const fiability = this.estimation()?.fiability ?? 0;

    return `Estimación sugerida de ${total} horas con una fiabilidad del ${fiability}%, validada con el desglose seleccionado.`;
  });

  readonly manualHours = signal<HourBreakdown>({
    hoursPlanning: 0,
    hoursAnalysis: 0,
    hoursDevelopment: 0,
    hoursTesting: 0,
  });

  readonly hourTypes = HOUR_TYPES;

  constructor() {
    effect(() => {
      const est = this.estimation();
      if (est) {
        this.manualHours.set({ ...(est.breakdown ?? est.suggested) });
      }
    });
  }

  readonly totalHours = computed(() => sumHourBreakdown(this.manualHours()));

  readonly suggestedTotalHours = computed(() => {
    const suggested = this.estimation()?.suggested;
    return suggested ? sumHourBreakdown(suggested) : 0;
  });

  readonly similarRequestsCount = computed(() => this.estimation()?.similarRequestsCount ?? 0);

  readonly fiabilityPercent = computed(() => {
    const estimation = this.estimation();
    if (estimation?.fiability !== undefined) {
      return Math.min(100, Math.max(0, estimation.fiability));
    }

    const suggested = estimation?.suggested;
    if (!suggested) {
      return 0;
    }

    const suggestedTotal = HOUR_TYPES.reduce((sum, type) => sum + (suggested[type] ?? 0), 0);
    if (suggestedTotal === 0) {
      return 0;
    }

    return Math.round((this.totalHours() / suggestedTotal) * 100);
  });

  readonly circleStyle = computed(() => {
    const percent = this.fiabilityPercent();
    return {
      '--circle-percent': `${percent}`,
    } as Record<string, string>;
  });

  labelFor(type: HourType): string {
    return HOUR_TYPE_LABELS[type];
  }

  suggestedFor(type: HourType): number {
    return this.estimation()?.suggested[type] ?? 0;
  }

  manualFor(type: HourType): number {
    return this.manualHours()[type];
  }

  increment(type: HourType): void {
    this.manualHours.update((current) => ({
      ...current,
      [type]: (current[type] ?? 0) + 1,
    }));
  }

  decrement(type: HourType): void {
    this.manualHours.update((current) => ({
      ...current,
      [type]: Math.max(0, (current[type] ?? 0) - 1),
    }));
  }

  validate(): void {
    this.stepCompleted.emit({ ...this.manualHours() });
  }

  onEdit(): void {
    this.stepEdit.emit();
  }
}
