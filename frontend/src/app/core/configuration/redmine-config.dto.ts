export interface RedmineCredentialResponse {
  id: number;
  redmineInstanceId: number;
  instanceName: string;
  apiKeyMasked: string;
  lastSyncAt: string | null;
}

export interface RedmineCredentialRequest {
  redmineUrl: string;
  plainApiKey?: string;
}

export type ConnectionStatus = 'none' | 'loading' | 'connected' | 'error';
