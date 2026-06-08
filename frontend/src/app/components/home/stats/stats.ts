import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { StatCard } from '../../shared/stat-card/stat-card';
import { StatMetric } from '../../../pages/home/home.models';

@Component({
  selector: 'app-stats',
  imports: [StatCard],
  templateUrl: './stats.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Stats {
  readonly title = input('Métricas de la plataforma');
  readonly statMetrics = input.required<StatMetric[]>();
}
