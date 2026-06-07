import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { Activity } from '../../components/home/activity/activity';
import { Welcome } from '../../components/home/welcome/welcome';
import { MetricsSummary } from '../../components/home/kpis/metrics-summary';
import { Requests } from '../../components/home/requests/requests';
import { Stats } from '../../components/home/stats/stats';
import { ActivityItem, KpiCard, StatMetric } from './home.models';
import { HomeService } from '../../services/home/home.service';

@Component({
  selector: 'app-home',
  imports: [Activity, Welcome, MetricsSummary, Requests, Stats],
  templateUrl: './home.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomePage {
  private readonly homeService = inject(HomeService);

  readonly kpis = signal<KpiCard[]>([]);
  readonly activities = signal<ActivityItem[]>([]);
  readonly statMetrics = signal<StatMetric[]>([]);
  readonly isLoading = signal(true);

  constructor() {
    this.homeService.loadData().subscribe({
      next: (data) => {
        this.kpis.set(data.kpis);
        this.activities.set(data.activities);
        this.statMetrics.set(data.statMetrics);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }
}
