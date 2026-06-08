import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';
import { RequestsApiService } from '../../core/requests/requests-api.service';
import { ImpactAnalysesApiService } from '../../core/impact-analyses/impact-analyses-api.service';
import { RequestResponse } from '../../core/requests/request.dto';
import { ImpactAnalysisResponse } from '../../core/impact-analyses/impact-analysis.dto';
import { ActivityItem, KpiCard } from '../../pages/home/home.models';
import { AuthService } from '../auth.service';

const PAGE = { page: 0, size: 200 } as const;
const NEUTRAL_KPI = { change: '—', changeType: 'neutral' as const };

const KPI_DEFS = [
  ['kpi-requests', 'Peticiones activas', 'requests', 'Casos abiertos esperando análisis de impacto.'],
  ['kpi-analysis', 'Análisis en curso', 'analysis', 'Estimaciones revisándose por el equipo funcional.'],
  ['kpi-documents', 'Documentos generados', 'documents', 'Informes, resúmenes y entregables listos para validar.']
] as const;

export interface HomeData {
  kpis: KpiCard[];
  activities: ActivityItem[];
}

@Injectable({ providedIn: 'root' })
export class HomeService {
  private readonly auth = inject(AuthService);
  private readonly requestsApi = inject(RequestsApiService);
  private readonly impactAnalysesApi = inject(ImpactAnalysesApiService);
  loadData(): Observable<HomeData> {
    if (!this.auth.isAuthenticated()) {
      return of(this.emptyData());
    }

    return forkJoin({
      requests: this.requestsApi.getAll({ ...PAGE, sort: 'createdDate,desc' }),
      analyses: this.impactAnalysesApi.getAll({ ...PAGE, sort: 'updatedAt,desc' })
    }).pipe(
      map(({ requests, analyses }) =>
        this.buildData(requests.content, analyses.content)
      ),
      catchError(() => of(this.emptyData()))
    );
  }

  private emptyData(): HomeData {
    return {
      kpis: this.buildKpis(0, 0, 0),
      activities: []
    };
  }

  private buildData(
    requests: RequestResponse[],
    rawAnalyses: ImpactAnalysisResponse[]
  ): HomeData {
    const requestIds = new Set(requests.map((r) => r.id));
    const requestsById = new Map(requests.map((r) => [r.id, r]));
    const analyses = rawAnalyses.filter((a) => requestIds.has(a.requestId));
    const active = requests.filter((r) => r.status?.toUpperCase() !== 'CLOSED');

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
      })
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
