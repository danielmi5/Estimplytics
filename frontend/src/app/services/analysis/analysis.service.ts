import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ComponentResponse } from '../../core/components/component.dto';
import { ComponentAnalysisResponse } from '../../core/component-analyses/component-analysis.dto';
import { EstimationResponse } from '../../core/estimations/estimation.dto';
import { Page } from '../../core/dtos/pagination.dto';
import { RequestResponse } from '../../core/requests/request.dto';
import { saveBlob } from '../../core/export/file-download.util';
import type { ImpactAnalysisDto, ImpactAnalysisResult } from '../../models/analysis/impact-analysis.model';
import type { EstimationResult } from '../../models/analysis/estimation.model';
import type { HourBreakdown } from '../../models/analysis/hour-breakdown.model';
import { sumHourBreakdown } from '../../models/analysis/hour-breakdown.model';

@Injectable({ providedIn: 'root' })
export class AnalysisService {
  private readonly http = inject(HttpClient);
  private readonly requestsUrl = `${environment.apiUrl}/requests`;
  private readonly impactAnalysesUrl = `${environment.apiUrl}/impact-analyses`;
  private readonly componentsUrl = `${environment.apiUrl}/components`;
  private readonly componentAnalysesUrl = `${environment.apiUrl}/component-analyses`;
  private readonly estimationsUrl = `${environment.apiUrl}/estimations`;

  lockRequest(requestId: string): Observable<RequestResponse> {
    return this.http.post<RequestResponse>(`${this.requestsUrl}/${requestId}/lock`, null);
  }

  findImpactAnalysisByRequestId(requestId: string): Observable<ImpactAnalysisResult | null> {
    return this.http
      .get<ImpactAnalysisResult>(`${this.impactAnalysesUrl}/request/${requestId}`)
      .pipe(catchError(() => of(null)));
  }

  submitImpactAnalysis(dto: ImpactAnalysisDto, existingId?: string | null): Observable<ImpactAnalysisResult> {
    if (existingId) {
      return this.http.put<ImpactAnalysisResult>(`${this.impactAnalysesUrl}/${existingId}`, dto);
    }

    return this.http.post<ImpactAnalysisResult>(this.impactAnalysesUrl, dto);
  }

  getArchitecturalComponents(): Observable<ArchitecturalComponent[]> {
    return this.http
      .get<Page<ComponentResponse>>(this.componentsUrl, {
        params: { page: '0', size: '200', sort: 'category,name,asc' },
      })
      .pipe(map((page) => page.content.filter((component) => component.active)));
  }

  getComponentAnalysesByAnalysisId(analysisId: string): Observable<ComponentAnalysisResponse[]> {
    return this.http
      .get<ComponentAnalysisResponse[]>(`${this.componentAnalysesUrl}/analysis/${analysisId}`)
      .pipe(catchError(() => of([])));
  }

  syncComponentSelections(
    analysisId: string,
    componentIds: readonly string[],
    existing: readonly ComponentAnalysisResponse[]
  ): Observable<void> {
    const existingByComponentId = new Map(existing.map((item) => [item.componentId, item.id]));
    const toAdd = componentIds.filter((id) => !existingByComponentId.has(id));
    const toRemove = existing.filter((item) => !componentIds.includes(item.componentId));

    const requests: Observable<unknown>[] = [
      ...toAdd.map((componentId) =>
        this.http.post<ComponentAnalysisResponse>(this.componentAnalysesUrl, {
          analysisId,
          componentId,
        })
      ),
      ...toRemove.map((item) => this.http.delete<void>(`${this.componentAnalysesUrl}/${item.id}`)),
    ];

    if (requests.length === 0) {
      return of(undefined);
    }

    return forkJoin(requests).pipe(map(() => undefined));
  }

  findEstimationByAnalysisId(analysisId: string): Observable<EstimationResult | null> {
    return this.findEstimationByAnalysisIdInternal(analysisId);
  }

  getEstimation(analysisId: string, versionNumber: number): Observable<EstimationResult> {
    return this.findEstimationByAnalysisIdInternal(analysisId).pipe(
      switchMap((existing) =>
        existing ? of(existing) : this.createEstimation(analysisId, versionNumber)
      )
    );
  }

  validateEstimation(
    estimationId: string,
    versionNumber: number,
    breakdown: HourBreakdown,
    fiability: number
  ): Observable<EstimationResult> {
    const totalHours = sumHourBreakdown(breakdown);

    return this.http
      .put<EstimationResponse>(`${this.estimationsUrl}/${estimationId}`, {
        versionNumber,
        fiability,
        hoursPlanning: breakdown.hoursPlanning,
        hoursAnalysis: breakdown.hoursAnalysis,
        hoursDevelopment: breakdown.hoursDevelopment,
        hoursTesting: breakdown.hoursTesting,
        totalHours,
        actualHoursFeedback: totalHours,
        updatedAt: new Date().toISOString(),
      })
      .pipe(map((response) => this.toEstimationResult(response)));
  }

  downloadAnalysisDoc(analysisId: string): Observable<Blob> {
    return this.http.get(`${this.impactAnalysesUrl}/${analysisId}/export/docx`, {
      responseType: 'blob',
    });
  }

  downloadPlanningDoc(estimationId: string): Observable<Blob> {
    return this.http.get(`${this.estimationsUrl}/${estimationId}/export/excel`, {
      responseType: 'blob',
    });
  }

  triggerAnalysisDownload(analysisId: string, filename: string): Observable<void> {
    return this.downloadAnalysisDoc(analysisId).pipe(
      map((blob) => {
        saveBlob(blob, filename);
      })
    );
  }

  triggerPlanningDownload(estimationId: string, filename: string): Observable<void> {
    return this.downloadPlanningDoc(estimationId).pipe(
      map((blob) => {
        saveBlob(blob, filename);
      })
    );
  }

  private createEstimation(analysisId: string, versionNumber: number): Observable<EstimationResult> {
    return this.http
      .post<EstimationResponse>(this.estimationsUrl, {
        analysisId,
        versionNumber,
        updatedAt: new Date().toISOString(),
      })
      .pipe(map((response) => this.toEstimationResult(response)));
  }

  private findEstimationByAnalysisIdInternal(analysisId: string): Observable<EstimationResult | null> {
    return this.http
      .get<EstimationResponse>(`${this.estimationsUrl}/analysis/${analysisId}`)
      .pipe(
        map((response) => this.toEstimationResult(response)),
        catchError(() => of(null))
      );
  }

  private toEstimationResult(response: EstimationResponse): EstimationResult {
    const breakdown: HourBreakdown = {
      hoursPlanning: response.hoursPlanning ?? 0,
      hoursAnalysis: response.hoursAnalysis ?? 0,
      hoursDevelopment: response.hoursDevelopment ?? 0,
      hoursTesting: response.hoursTesting ?? 0,
    };

    return {
      id: response.id,
      analysisId: response.analysisId,
      versionNumber: response.versionNumber,
      fiability: response.fiability ?? 0,
      suggested: { ...breakdown },
      breakdown,
      totalHours: response.totalHours ?? sumHourBreakdown(breakdown),
      similarRequestsCount: response.similarRequestsCount ?? 0,
    };
  }
}

type ArchitecturalComponent = ComponentResponse;
