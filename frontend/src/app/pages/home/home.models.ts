export interface KpiCard {
  id: string;
  label: string;
  value: string;
  change: string;
  changeType: 'positive' | 'negative' | 'neutral';
  icon: 'requests' | 'analysis' | 'documents';
  description: string;
}

export interface PendingRequest {
  id: string;
  title: string;
  source: string;
  date: string;
  priority: 'high' | 'medium' | 'low';
}

export interface ActivityItem {
  id: string;
  type: 'document' | 'estimation' | 'analysis';
  title: string;
  description: string;
  user: string;
  time: string;
}

export interface StatMetric {
  id: string;
  label: string;
  value: number;
  maxValue: number;
  color: string;
}