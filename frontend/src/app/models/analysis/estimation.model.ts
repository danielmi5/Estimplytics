import type { HourBreakdown } from './hour-breakdown.model';

export interface EstimationResult {
  id: string;
  analysisId: string;
  versionNumber: number;
  fiability: number;
  suggested: HourBreakdown;
  breakdown: HourBreakdown;
  totalHours: number;
  similarRequestsCount?: number;
}

export interface EstimationViewModel {
  fiability: number;
  suggested: HourBreakdown;
  breakdown?: HourBreakdown;
  estimationId?: string;
  similarRequestsCount?: number;
}
