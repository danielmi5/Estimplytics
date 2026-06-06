export interface EstimationRequest {
  analysisId: string;
  versionNumber: number;
  fiability?: number;
  hoursPlanning?: number;
  hoursAnalysis?: number;
  hoursDevelopment?: number;
  hoursTesting?: number;
  totalHours?: number;
  actualHoursFeedback?: number;
  justification?: string;
  updatedAt: string;
}

export interface EstimationResponse {
  id: string;
  analysisId: string;
  versionNumber: number;
  fiability?: number;
  hoursPlanning?: number;
  hoursAnalysis?: number;
  hoursDevelopment?: number;
  hoursTesting?: number;
  totalHours?: number;
  actualHoursFeedback?: number;
  justification?: string;
  createdAt?: string;
  updatedAt: string;
  similarRequestsCount?: number;
}

export interface EstimationUpdate {
  versionNumber: number;
  fiability?: number;
  hoursPlanning?: number;
  hoursAnalysis?: number;
  hoursDevelopment?: number;
  hoursTesting?: number;
  totalHours?: number;
  actualHoursFeedback?: number;
  justification?: string;
  updatedAt: string;
}
