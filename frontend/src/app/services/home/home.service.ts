import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';
import { RequestsApiService } from '../../core/requests/requests-api.service';
import { ImpactAnalysesApiService } from '../../core/impact-analyses/impact-analyses-api.service';
import { EstimationsApiService } from '../../core/estimations/estimations-api.service';
import { RequestResponse } from '../../core/requests/request.dto';
import { ImpactAnalysisResponse } from '../../core/impact-analyses/impact-analysis.dto';
import { EstimationResponse } from '../../core/estimations/estimation.dto';
import { ActivityItem, KpiCard, StatMetric } from '../../pages/home/home.models';
import { AuthService } from '../auth.service';

const PAGE = { page: 0, size: 200 } as const;
const RESPONSE_TARGET_HOURS = 48;
const NEUTRAL_KPI = { change: '—', changeType: 'neutral' as const };

const KPI_DEFS = [
  ['kpi-requests', 'Peticiones activas', 'requests', 'Casos abiertos esperando análisis de impacto.'],
  ['kpi-analysis', 'Análisis en curso', 'analysis', 'Estimaciones revisándose por el equipo funcional.'],
  ['kpi-documents', 'Documentos generados', 'documents', 'Informes, resúmenes y entregables listos para validar.']
] as const;

const STAT_DEFS = [
  ['Precisión de estimaciones', 'linear-gradient(90deg, #2f6bff 0%, #8cc0ff 100%)'],
  ['Cobertura de análisis', 'linear-gradient(90deg, #0f9d7a 0%, #72d6b6 100%)'],
  ['Tiempo de respuesta', 'linear-gradient(90deg, #ef7d32 0%, #f7b267 100%)'],
  ['Documentos generados', 'linear-gradient(90deg, #6c63ff 0%, #8d89ff 100%)']
] as const;

export interface HomeData {
  kpis: KpiCard[];
  activities: ActivityItem[];
  statMetrics: StatMetric[];
}

const pct = (count: number, total: number) => ((total) ? (count / total) * 100 : 0);

@Injectable({ providedIn: 'root' })
export class HomeService {
  private readonly auth = inject(AuthService);
  private readonly requestsApi = inject(RequestsApiService);
  private readonly impactAnalysesApi = inject(ImpactAnalysesApiService);
  private readonly estimationsApi = inject(EstimationsApiService);

  loadData(): Observable<HomeData> {
    if (!this.auth.isAuthenticated()) {
      return of(this.emptyData());
    }

    return forkJoin({
      requests: this.requestsApi.getAll({ ...PAGE, sort: 'createdDate,desc' }),
      analyses: this.impactAnalysesApi.getAll({ ...PAGE, sort: 'updatedAt,desc' }),
      estimations: this.estimationsApi.getAll({ ...PAGE, sort: 'updatedAt,desc' })
    }).pipe(
      map(({ requests, analyses, estimations }) =>
        this.buildData(requests.content, analyses.content, estimations.content)
      ),
      catchError(() => of(this.emptyData()))
    );
  }

  private emptyData(): HomeData {
    return {
      kpis: this.buildKpis(0, 0, 0),
      activities: [],
      statMetrics: this.buildStatMetrics(0, 0, 0, 0)
    };
  }

  private buildData(
    requests: RequestResponse[],
    rawAnalyses: ImpactAnalysisResponse[],
    rawEstimations: EstimationResponse[]
  ): HomeData {
    const requestIds = new Set(requests.map((r) => r.id));
    const requestsById = new Map(requests.map((r) => [r.id, r]));
    const analyses = rawAnalyses.filter((a) => requestIds.has(a.requestId));
    const analysisIds = new Set(analyses.map((a) => a.id));
    const analysisByRequestId = new Map(analyses.map((a) => [a.requestId, a]));
    const estimations = rawEstimations.filter((e) => analysisIds.has(e.analysisId));
    const estimationAnalysisIds = new Set(estimations.map((e) => e.analysisId));
    const analysedRequestIds = new Set(analyses.map((a) => a.requestId));
    const active = requests.filter((r) => r.status?.toUpperCase() !== 'CLOSED');

    const withBothDocuments = requests.filter((r) => {
      const analysis = analysisByRequestId.get(r.id);
      return analysis != null && estimationAnalysisIds.has(analysis.id);
    }).length;

    const responseHours = requests
      .map((r) => {
        const end = r.analysedAt ?? analysisByRequestId.get(r.id)?.updatedAt;
        const start = Date.parse(r.createdDate);
        const finish = (end) ? Date.parse(end) : NaN;
        return ((!end || Number.isNaN(start) || Number.isNaN(finish) || finish < start) ? null : (finish - start) / 3600000);
      })
      .filter((hours): hours is number => hours != null);

    const avgResponseHours = (responseHours.length) ? (responseHours.reduce((sum, hours) => sum + hours, 0) / responseHours.length) : 0;

    return {
      kpis: this.buildKpis(
        active.length,
        active.filter((r) => r.analysedById != null).length,
        analyses.length
      ),
      activities: analyses.slice(0, 4).map((analysis) => {
        const request = requestsById.get(analysis.requestId);
        const code = request?.originRequestCode ?? analysis.requestId.slice(0, 8);
        const title = ((typeof analysis.documentData?.['title'] === 'string') ? analysis.documentData['title'] : `Análisis ${code}`);

        return {
          id: analysis.id,
          type: 'analysis' as const,
          title,
          description: request?.title ?? 'Análisis de impacto registrado en la plataforma.',
          user: request?.analysedByName ?? request?.authorName ?? 'Equipo',
          time: relativeTime(analysis.updatedAt)
        };
      }),
      statMetrics: this.buildStatMetrics(
        this.estimationPrecision(estimations),
        pct(analysedRequestIds.size, requests.length),
        Math.max(0, Math.min(100, ((RESPONSE_TARGET_HOURS - avgResponseHours) / RESPONSE_TARGET_HOURS) * 100)),
        pct(withBothDocuments, requests.length)
      )
    };
  }

  private buildKpis(active: number, inProgress: number, documents: number): KpiCard[] {
    const values = [active, inProgress, documents];
    return KPI_DEFS.map(([id, label, icon, description], index) => ({
      id,
      label,
      value: String(values[index]),
      icon,
      description,
      ...NEUTRAL_KPI
    }));
  }

  private buildStatMetrics(
    precision: number,
    coverage: number,
    responseTime: number,
    documents: number
  ): StatMetric[] {
    const values = [precision, coverage, responseTime, documents];
    return STAT_DEFS.map(([label, color], index) => ({
      id: `metric-${index + 1}`,
      label,
      value: Math.round(values[index] ?? 0),
      maxValue: 100,
      color
    }));
  }

  private estimationPrecision(estimations: EstimationResponse[]): number {
    if (!estimations.length) {
      return 0;
    }

    const total = estimations.reduce((sum, estimation) => {
      const { totalHours, actualHoursFeedback, fiability = 0 } = estimation;
      if (totalHours != null && actualHoursFeedback != null && actualHoursFeedback > 0) {
        return sum + Math.max(0, 100 - (Math.abs(totalHours - actualHoursFeedback) / actualHoursFeedback) * 100);
      }
      return sum + fiability;
    }, 0);

    return total / estimations.length;
  }
}

function relativeTime(isoDate: string): string {
  const diffMin = Math.floor((Date.now() - Date.parse(isoDate)) / 60000);
  if (Number.isNaN(diffMin) || diffMin < 0) {
    return 'Fecha desconocida';
  }
  if (diffMin < 1) {
    return 'Hace un momento';
  }
  if (diffMin < 60) {
    return `Hace ${diffMin} min`;
  }

  const hours = Math.floor(diffMin / 60);
  if (hours < 24) {
    return (hours === 1) ? 'Hace 1 hora' : `Hace ${hours} horas`;
  }

  const days = Math.floor(hours / 24);
  return (days === 1) ? 'Hace 1 día' : `Hace ${days} días`;
}
