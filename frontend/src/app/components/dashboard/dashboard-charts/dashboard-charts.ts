import { DecimalPipe } from '@angular/common';
import { Component, ChangeDetectionStrategy, computed, input } from '@angular/core';
import { CircleChartData, LineChartPoint } from '../../../pages/dashboard/dashboard.models';

interface ChartMarker {
  label: string;
  cx: number;
  cy: number;
}

@Component({
  selector: 'app-dashboard-charts',
  imports: [DecimalPipe],
  templateUrl: './dashboard-charts.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardCharts {
  private static readonly CHART_WIDTH = 280;
  private static readonly CHART_HEIGHT = 120;

  readonly lineChart = input.required<LineChartPoint[]>();
  readonly circle = input.required<CircleChartData>();

  readonly maxLineValue = computed(() => {
    const values = this.lineChart().map((point) => point.value);
    return Math.max(...values, 1);
  });

  readonly linePoints = computed(() => {
    const points = this.lineChart();
    const max = this.maxLineValue();
    const step = this.chartStep(points.length);

    return points
      .map((point, index) => {
        const x = index * step;
        const y = this.chartY(point.value, max);
        return `${x},${y}`;
      })
      .join(' ');
  });

  readonly trendPoints = computed(() => {
    const points = this.lineChart();
    const max = this.maxLineValue();
    const width = DashboardCharts.CHART_WIDTH;
    const first = points[0]?.value ?? 0;
    const last = points.at(-1)?.value ?? 0;

    return `0,${this.chartY(first, max)} ${width},${this.chartY(last, max)}`;
  });

  readonly chartMarkers = computed<ChartMarker[]>(() => {
    const points = this.lineChart();
    const max = this.maxLineValue();
    const step = this.chartStep(points.length);

    return points.map((point, index) => ({
      label: point.label,
      cx: index * step,
      cy: this.chartY(point.value, max)
    }));
  });

  readonly circleGradient = computed(() => {
    const segments = this.circle().segments;
    const total = segments.reduce((sum, segment) => sum + segment.value, 0) || 1;
    let current = 0;

    return segments
      .map((segment) => {
        const start = (current / total) * 100;
        current += segment.value;
        const end = (current / total) * 100;
        return `${segment.color} ${start}% ${end}%`;
      })
      .join(', ');
  });

  readonly circleAriaLabel = computed(() => `${this.circle().completionRate}% completado`);

  private chartStep(pointCount: number): number {
    return DashboardCharts.CHART_WIDTH / Math.max(pointCount - 1, 1);
  }

  private chartY(value: number, max: number): number {
    return DashboardCharts.CHART_HEIGHT - (value / max) * DashboardCharts.CHART_HEIGHT;
  }
}
