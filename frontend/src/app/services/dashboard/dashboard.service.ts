import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';
import { RequestsApiService } from '../../core/requests/requests-api.service';
import { ImpactAnalysesApiService } from '../../core/impact-analyses/impact-analyses-api.service';
import { EstimationsApiService } from '../../core/estimations/estimations-api.service';
import { RequestResponse } from '../../core/requests/request.dto';
import { ImpactAnalysisResponse } from '../../core/impact-analyses/impact-analysis.dto';
import { EstimationResponse } from '../../core/estimations/estimation.dto';
import { StatMetric } from '../../pages/home/home.models';
import {
  DashboardData,
  DashboardSummaryMetric,
  CircleChartData,
  CircleSegment,
  LineChartPoint
} from '../../pages/dashboard/dashboard.models';
import { AuthService } from '../auth.service';

const PAGE = { page: 0, size: 200 } as const;
const RESPONSE_TARGET_HOURS = 48;

const STAT_DEFS = [
  ['Precisión de estimaciones', 'linear-gradient(90deg, #2f6bff 0%, #8cc0ff 100%)'],
  ['Cobertura de análisis', 'linear-gradient(90deg, #0f9d7a 0%, #72d6b6 100%)'],
  ['Tiempo de respuesta', 'linear-gradient(90deg, #ef7d32 0%, #f7b267 100%)'],
  ['Documentos generados', 'linear-gradient(90deg, #6c63ff 0%, #8d89ff 100%)']
] as const;

const DONUT_COLORS = {
  completed: 'var(--color-category-4)',
  inProgress: 'var(--color-category-3)',
  notStarted: 'var(--color-neutral-3)'
} as const;

const pct = (count: number, total: number) => ((total) ? (count / total) * 100 : 0);
const formatNumber = (value: number) => value.toLocaleString('es-ES');

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly auth = inject(AuthService);
  private readonly requestsApi = inject(RequestsApiService);
  private readonly impactAnalysesApi = inject(ImpactAnalysesApiService);
  private readonly estimationsApi = inject(EstimationsApiService);

  loadData(): Observable<DashboardData> {
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

  private emptyData(): DashboardData {
    return {
      summary: this.buildSummary(0, 0, 0, 0, 0),
      lineChart: this.buildLineChart([]),
      circle: this.buildCircle(0, 0, 0),
      statMetrics: this.buildStatMetrics(0, 0, 0, 0)
    };
  }

  private buildData(
    requests: RequestResponse[],
    rawAnalyses: ImpactAnalysisResponse[],
    rawEstimations: EstimationResponse[]
  ): DashboardData {
    const requestIds = new Set(requests.map((request) => request.id));
    const analyses = rawAnalyses.filter((analysis) => requestIds.has(analysis.requestId));
    const analysisIds = new Set(analyses.map((analysis) => analysis.id));
    const analysisByRequestId = new Map(analyses.map((analysis) => [analysis.requestId, analysis]));
    const estimations = rawEstimations.filter((estimation) => analysisIds.has(estimation.analysisId));
    const estimationAnalysisIds = new Set(estimations.map((estimation) => estimation.analysisId));
    const analysedRequestIds = new Set(analyses.map((analysis) => analysis.requestId));

    const withBothDocuments = requests.filter((request) => {
      const analysis = analysisByRequestId.get(request.id);
      return analysis != null && estimationAnalysisIds.has(analysis.id);
    }).length;

    const responseHours = requests
      .map((request) => {
        const end = request.analysedAt ?? analysisByRequestId.get(request.id)?.updatedAt;
        const start = Date.parse(request.createdDate);
        const finish = (end) ? Date.parse(end) : NaN;
        return ((!end || Number.isNaN(start) || Number.isNaN(finish) || finish < start)
          ? null
          : (finish - start) / 3600000);
      })
      .filter((hours): hours is number => hours != null);

    const avgResponseHours = (responseHours.length)
      ? responseHours.reduce((sum, hours) => sum + hours, 0) / responseHours.length
      : 0;

    const criticalOpen = requests.filter((request) =>
      request.status?.toUpperCase() !== 'CLOSED'
      && request.priority?.toUpperCase() === 'HIGH'
    ).length;

    const coverage = Math.round(pct(analysedRequestIds.size, requests.length));
    const variation = this.estimationVariation(estimations);
    const recentGrowth = this.recentGrowth(requests);
    const { completed, inProgress, notStarted } = this.requestStatuses(
      requests,
      analysisByRequestId,
      estimationAnalysisIds
    );

    return {
      summary: this.buildSummary(
        requests.length,
        variation,
        criticalOpen,
        coverage,
        recentGrowth
      ),
      lineChart: this.buildLineChart(analyses),
      circle: this.buildCircle(completed, inProgress, notStarted),
      statMetrics: this.buildStatMetrics(
        this.estimationPrecision(estimations),
        pct(analysedRequestIds.size, requests.length),
        Math.max(0, Math.min(100, ((RESPONSE_TARGET_HOURS - avgResponseHours) / RESPONSE_TARGET_HOURS) * 100)),
        pct(withBothDocuments, requests.length)
      )
    };
  }

  private buildSummary(
    totalRequests: number,
    variation: number,
    criticalOpen: number,
    coverage: number,
    recentGrowth: number
  ): DashboardSummaryMetric[] {
    const growthLabel = (recentGrowth >= 0)
      ? `+${recentGrowth.toFixed(1)}%`
      : `${recentGrowth.toFixed(1)}%`;

    return [
      {
        id: 'summary-total',
        label: 'Peticiones totales',
        value: formatNumber(totalRequests),
        tone: 'category-2',
        change: growthLabel,
        changeType: (recentGrowth >= 0) ? 'positive' : 'negative'
      },
      {
        id: 'summary-variation',
        label: 'Variación estimada',
        value: `${variation.toFixed(1)}%`,
        tone: 'category-1'
      },
      {
        id: 'summary-critical',
        label: 'Peticiones críticas abiertas',
        value: formatNumber(criticalOpen),
        tone: 'category-4'
      },
      {
        id: 'summary-coverage',
        label: 'Porcentaje aproximado del análisis',
        value: `${coverage}%`,
        tone: 'category-3',
        progress: coverage
      }
    ];
  }

  private buildLineChart(analyses: ImpactAnalysisResponse[]): LineChartPoint[] {
    const now = Date.now();
    const dayMs = 86400000;
    const buckets = [
      { label: 'D-30', from: 30, to: Infinity },
      { label: 'D-15', from: 15, to: 30 },
      { label: 'D-0', from: 0, to: 15 }
    ];

    return buckets.map(({ label, from, to }) => ({
      label,
      value: analyses.filter((analysis) => {
        const ageDays = (now - Date.parse(analysis.updatedAt)) / dayMs;
        return ageDays >= from && ageDays < to;
      }).length
    }));
  }

  private buildCircle(completed: number, inProgress: number, notStarted: number): CircleChartData {
    const total = completed + inProgress + notStarted;
    const completionRate = Math.round(pct(completed, total));
    const segments: CircleSegment[] = [
      { id: 'completed', label: 'Completadas', value: completed, color: DONUT_COLORS.completed },
      { id: 'in-progress', label: 'En progreso', value: inProgress, color: DONUT_COLORS.inProgress },
      { id: 'not-started', label: 'Sin iniciar', value: notStarted, color: DONUT_COLORS.notStarted }
    ];

    return { completionRate, segments };
  }

  private requestStatuses(
    requests: RequestResponse[],
    analysisByRequestId: Map<string, ImpactAnalysisResponse>,
    estimationAnalysisIds: Set<string>
  ) {
    let completed = 0;
    let inProgress = 0;
    let notStarted = 0;

    for (const request of requests) {
      if (request.status?.toUpperCase() === 'CLOSED') {
        completed += 1;
        continue;
      }

      const analysis = analysisByRequestId.get(request.id);
      if (!analysis) {
        notStarted += 1;
        continue;
      }

      if (estimationAnalysisIds.has(analysis.id)) {
        completed += 1;
      } else {
        inProgress += 1;
      }
    }

    return { completed, inProgress, notStarted };
  }

  private recentGrowth(requests: RequestResponse[]): number {
    if (!requests.length) {
      return 0;
    }

    const now = Date.now();
    const dayMs = 86400000;
    const recent = requests.filter((request) => (now - Date.parse(request.createdDate)) / dayMs <= 30).length;
    const previous = requests.filter((request) => {
      const ageDays = (now - Date.parse(request.createdDate)) / dayMs;
      return ageDays > 30 && ageDays <= 60;
    }).length;

    if (!previous) {
      return recent > 0 ? 100 : 0;
    }

    return ((recent - previous) / previous) * 100;
  }

  private estimationVariation(estimations: EstimationResponse[]): number {
    if (!estimations.length) {
      return 0;
    }

    const total = estimations.reduce((sum, estimation) => {
      const { totalHours, actualHoursFeedback } = estimation;
      if (totalHours == null || actualHoursFeedback == null || actualHoursFeedback <= 0) {
        return sum;
      }

      return sum + Math.abs(((totalHours - actualHoursFeedback) / actualHoursFeedback) * 100);
    }, 0);

    return total / estimations.length;
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
