import { StatMetric } from '../home/home.models';

export type DashboardSummaryTone = 'category-2' | 'category-1' | 'category-4' | 'category-3';

export interface DashboardSummaryMetric {
  id: string;
  label: string;
  value: string;
  tone: DashboardSummaryTone;
  change?: string;
  changeType?: 'positive' | 'negative' | 'neutral';
  progress?: number;
}

export interface LineChartPoint {
  label: string;
  value: number;
}

export interface CircleSegment {
  id: string;
  label: string;
  value: number;
  color: string;
}

export interface CircleChartData {
  completionRate: number;
  segments: CircleSegment[];
}

export interface DashboardData {
  summary: DashboardSummaryMetric[];
  lineChart: LineChartPoint[];
  circle: CircleChartData;
  statMetrics: StatMetric[];
}
