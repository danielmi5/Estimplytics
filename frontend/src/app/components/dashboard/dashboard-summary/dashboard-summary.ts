import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { DashboardSummaryMetric } from '../../../pages/dashboard/dashboard.models';

@Component({
  selector: 'app-dashboard-summary',
  templateUrl: './dashboard-summary.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardSummary {
  readonly metrics = input.required<DashboardSummaryMetric[]>();
}
