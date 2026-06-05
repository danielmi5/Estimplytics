export interface RequestRequest {
  projectId: string;
  demandType?: string;
  title: string;
  description?: string;
  status: string;
  priority: string;
  startDate?: string;
  endDate?: string;
  doneRatio?: number;
  estimatedHours?: number;
  spentHours?: number;
}

export interface RequestResponse {
  id: string;
  projectId: string;
  redmineId?: number;
  originRequestCode?: string;
  projectName?: string;
  demandType?: string;
  title: string;
  description?: string;
  status: string;
  priority: string;
  assigneeName?: string;
  authorName?: string;
  startDate?: string;
  endDate?: string;
  redmineCreatedDate?: string;
  redmineUpdatedDate?: string;
  redmineClosedDate?: string;
  doneRatio?: number;
  estimatedHours?: number;
  spentHours?: number;
  createdDate: string;
  ownerId?: string;
  analysedById?: string;
  analysedByName?: string;
  analysedAt?: string;
}

export interface RequestUpdate {
  title?: string;
  description?: string;
  status?: string;
}
