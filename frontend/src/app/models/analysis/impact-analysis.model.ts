export interface ImpactAnalysisDocumentData {
  descripcionAbreviada: string;
  descripcionImpacto: string;
  descripcionSolucion: string;
  requisitosFuncionales: string;
  pruebas: string;
  complexity: string;
  versionNumber: number;
}

export interface ImpactAnalysisDto {
  requestId?: string;
  userId?: string;
  versionNumber: number;
  complexity: string;
  documentData: Omit<ImpactAnalysisDocumentData, 'complexity' | 'versionNumber'>;
}

export interface ImpactAnalysisResult {
  id: string;
  requestId: string;
  userId: string;
  versionNumber: number;
  complexity: string;
  documentData: Record<string, unknown>;
  updatedAt: string;
}
