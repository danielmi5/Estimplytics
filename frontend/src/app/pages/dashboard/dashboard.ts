import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { Sidebar } from '../../components/layout/sidebar/sidebar';
import { DashboardSummary } from '../../components/dashboard/dashboard-summary/dashboard-summary';
import { DashboardCharts } from '../../components/dashboard/dashboard-charts/dashboard-charts';
import { Stats } from '../../components/home/stats/stats';
import { DashboardService } from '../../services/dashboard/dashboard.service';
import {
  DashboardSummaryMetric,
  CircleChartData,
  LineChartPoint
} from './dashboard.models';
import { StatMetric } from '../home/home.models';

@Component({
  selector: 'app-dashboard-page',
  imports: [Sidebar, DashboardSummary, DashboardCharts, Stats],
  templateUrl: './dashboard.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardPage {
  private readonly dashboardService = inject(DashboardService);

  readonly summary = signal<DashboardSummaryMetric[]>([]);
  readonly lineChart = signal<LineChartPoint[]>([]);
  readonly circle = signal<CircleChartData>({ completionRate: 0, segments: [] });
  readonly statMetrics = signal<StatMetric[]>([]);
  readonly isLoading = signal(true);

  constructor() {
    this.dashboardService.loadData().subscribe({
      next: (data) => {
        this.summary.set(data.summary);
        this.lineChart.set(data.lineChart);
        this.circle.set(data.circle);
        this.statMetrics.set(data.statMetrics);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }
}
