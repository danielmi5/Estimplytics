import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { finalize, forkJoin, of, switchMap } from 'rxjs';
import { Sidebar } from '../../components/layout/sidebar/sidebar';
import { StepAnalysis } from '../../components/analysis/step-analysis/step-analysis';
import { StepComponents } from '../../components/analysis/step-components/step-components';
import { StepEstimation } from '../../components/analysis/step-estimation/step-estimation';
import { StepFiles } from '../../components/analysis/step-files/step-files';
import type {
  ArchitecturalComponent,
  EstimationViewModel,
  HourBreakdown,
  ImpactAnalysisDocumentData,
} from '../../components/analysis/analysis.models';
import type { ImpactAnalysisResult } from '../../models/analysis/impact-analysis.model';
import type { EstimationResult } from '../../models/analysis/estimation.model';
import { ComponentAnalysisResponse } from '../../core/component-analyses/component-analysis.dto';
import { AppStateService } from '../../services/app-state.service';
import { AnalysisService } from '../../services/analysis/analysis.service';
import { getApiErrorMessage } from '../../services/analysis/analysis-error.util';
import { STEP_ORDER, type StepId, type StepState } from './step-state';

const INITIAL_STEP_STATES: Record<StepId, StepState> = {
  analysis: 'active',
  components: 'inactive',
  estimation: 'inactive',
  files: 'inactive',
};

@Component({
  selector: 'app-analysis-page',
  imports: [Sidebar, StepAnalysis, StepComponents, StepEstimation, StepFiles],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="analysis">
      <app-sidebar></app-sidebar>

      <section class="analysis__content">
        <div class="o-workflow">
          <div class="o-workflow__container">
            <h1 class="analysis__title">Análisis de impacto</h1>

            @if (!requestId()) {
              <p class="analysis__error" role="alert">
                Falta el identificador de petición. Accede desde una petición con ?requestId=...
              </p>
            }

            @if (isInitializing()) {
              <p class="analysis__error">Preparando análisis...</p>
            }

            <app-step-analysis
              [state]="stepStates().analysis"
              [summary]="analysisSummary()"
              [isSubmitting]="isSubmittingAnalysis()"
              [submitError]="analysisSubmitError()"
              (stepCompleted)="onAnalysisCompleted($event)"
              (stepEdit)="editStep('analysis')"
            />

            <app-step-components
              [state]="stepStates().components"
              [components]="architecturalComponents()"
              [isLoading]="isLoadingComponents()"
              [errorMessage]="componentsLoadError()"
              [selectedIds]="selectedComponentIds()"
              [selectedNames]="selectedComponentNames()"
              (selectionChange)="onComponentSelectionChange($event)"
              (stepCompleted)="onComponentsCompleted($event)"
              (stepEdit)="editStep('components')"
            />

            <app-step-estimation
              [state]="stepStates().estimation"
              [estimation]="estimationViewModel()"
              [approvedBreakdown]="approvedBreakdown()"
              [isLoading]="isLoadingEstimation()"
              [isValidating]="isValidatingEstimation()"
              [errorMessage]="estimationValidateError() ?? estimationLoadError()"
              (stepCompleted)="onEstimationCompleted($event)"
              (stepEdit)="editStep('estimation')"
            />

            <app-step-files
              [state]="stepStates().files"
              [canDownload]="canDownload()"
              [versionDisplay]="versionDisplay()"
              [isDownloadingAnalysis]="isDownloadingAnalysis()"
              [isDownloadingPlanning]="isDownloadingPlanning()"
              [analysisDownloaded]="analysisDownloaded()"
              [planningDownloaded]="planningDownloaded()"
              (downloadAnalysis)="onDownloadAnalysis()"
              (downloadPlanning)="onDownloadPlanning()"
              (stepCompleted)="completeStep('files')"
              (stepEdit)="editStep('files')"
            />
          </div>
        </div>
      </section>
    </div>
  `,
})
export class AnalysisPage {
  private readonly analysisService = inject(AnalysisService);
  private readonly appState = inject(AppStateService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly requestId = signal<string | null>(null);
  readonly originRequestCode = signal<string | null>(null);
  readonly analysisId = signal<string | null>(null);
  readonly estimationId = signal<string | null>(null);

  readonly stepStates = signal<Record<StepId, StepState>>({ ...INITIAL_STEP_STATES });
  readonly analysisSummary = signal<ImpactAnalysisDocumentData | null>(null);
  readonly architecturalComponents = signal<ArchitecturalComponent[]>([]);
  readonly savedComponentAnalyses = signal<readonly ComponentAnalysisResponse[]>([]);
  readonly selectedComponentIds = signal<readonly string[]>([]);
  readonly selectedComponentNames = computed(() => {
    const catalog = this.architecturalComponents();
    return this.selectedComponentIds().map(
      (id) => catalog.find((component) => component.id === id)?.name ?? id
    );
  });
  readonly estimationViewModel = signal<EstimationViewModel | null>(null);
  readonly approvedBreakdown = signal<HourBreakdown | null>(null);
  readonly analysisDownloaded = signal(false);
  readonly planningDownloaded = signal(false);

  readonly isInitializing = signal(false);
  readonly isSubmittingAnalysis = signal(false);
  readonly analysisSubmitError = signal<string | null>(null);
  readonly isLoadingComponents = signal(false);
  readonly componentsLoadError = signal<string | null>(null);
  readonly isLoadingEstimation = signal(false);
  readonly estimationLoadError = signal<string | null>(null);
  readonly isValidatingEstimation = signal(false);
  readonly estimationValidateError = signal<string | null>(null);
  readonly isDownloadingAnalysis = signal(false);
  readonly isDownloadingPlanning = signal(false);

  private readonly componentsLoaded = signal(false);
  private readonly estimationLoadAttempted = signal(false);

  readonly canDownload = computed(() => {
    const states = this.stepStates();
    return (
      states.analysis === 'completed' &&
      states.components === 'completed' &&
      states.estimation === 'completed' &&
      this.analysisId() !== null &&
      this.estimationId() !== null
    );
  });

  readonly versionDisplay = computed(() => {
    const summary = this.analysisSummary();
    if (!summary) {
      return '1.0';
    }
    return `${summary.versionNumber}.0`;
  });

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const requestId = params.get('requestId');
      this.requestId.set(requestId);
      if (requestId) {
        this.initializeRequest(requestId);
      }
    });

    effect(() => {
      if ((this.stepStates().components === 'active' || this.selectedComponentIds().length > 0) &&!this.componentsLoaded()) {
        this.loadArchitecturalComponents();
      }
    });

    effect(() => {
      const states = this.stepStates();
      const analysisId = this.analysisId();
      if (
        states.estimation === 'active' &&
        analysisId &&
        !this.estimationViewModel() &&
        !this.estimationLoadAttempted()
      ) {
        this.loadEstimation(analysisId);
      }
    });
  }

  completeStep(id: StepId): void {
    const index = STEP_ORDER.indexOf(id);
    this.stepStates.update((states) => {
      const next: Record<StepId, StepState> = { ...states };
      next[id] = 'completed';
      const nextStepId = STEP_ORDER[index + 1];
      if (nextStepId) {
        next[nextStepId] = 'active';
      }
      return next;
    });
  }

  editStep(id: StepId): void {
    const index = STEP_ORDER.indexOf(id);
    this.stepStates.update((states) => {
      const next: Record<StepId, StepState> = { ...states };
      STEP_ORDER.forEach((stepId, stepIndex) => {
        if (stepIndex < index) {
          next[stepId] = 'completed';
        } else if (stepIndex === index) {
          next[stepId] = 'active';
        } else {
          next[stepId] = 'inactive';
        }
      });
      return next;
    });

    if (id === 'estimation') {
      this.estimationValidateError.set(null);
      this.estimationLoadError.set(null);
      this.estimationLoadAttempted.set(false);
    }
  }

  onAnalysisCompleted(data: ImpactAnalysisDocumentData): void {
    const requestId = this.requestId();
    const userId = this.appState.user()?.id;

    if (!requestId) {
      this.analysisSubmitError.set('No se ha indicado la petición asociada al análisis.');
      return;
    }

    if (!userId) {
      this.analysisSubmitError.set('Debes iniciar sesión para guardar el análisis.');
      return;
    }

    this.isSubmittingAnalysis.set(true);
    this.analysisSubmitError.set(null);

    this.analysisService
      .submitImpactAnalysis(
        {
          requestId,
          userId,
          versionNumber: data.versionNumber,
          complexity: data.complexity,
          documentData: {
            descripcionAbreviada: data.descripcionAbreviada,
            descripcionImpacto: data.descripcionImpacto,
            descripcionSolucion: data.descripcionSolucion,
            requisitosFuncionales: data.requisitosFuncionales,
            pruebas: data.pruebas,
          },
        },
        this.analysisId()
      )
      .pipe(
        finalize(() => this.isSubmittingAnalysis.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (result) => {
          this.analysisId.set(result.id);
          this.analysisSummary.set(data);
          this.completeStep('analysis');
        },
        error: (err: unknown) => {
          this.analysisSubmitError.set(
            getApiErrorMessage(err, 'No se pudo guardar el análisis de impacto.')
          );
        },
      });
  }

  onComponentSelectionChange(ids: readonly string[]): void {
    this.selectedComponentIds.set(ids);
  }

  onComponentsCompleted(ids: readonly string[]): void {
    const analysisId = this.analysisId();
    if (!analysisId) {
      return;
    }

    this.onComponentSelectionChange(ids);
    this.componentsLoadError.set(null);

    this.analysisService
      .syncComponentSelections(analysisId, ids, this.savedComponentAnalyses())
      .pipe(
        switchMap(() => this.analysisService.getComponentAnalysesByAnalysisId(analysisId)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (saved) => {
          this.savedComponentAnalyses.set(saved);
          this.completeStep('components');
        },
        error: (err: unknown) => {
          this.componentsLoadError.set(
            getApiErrorMessage(err, 'No se pudieron guardar los componentes seleccionados.')
          );
        },
      });
  }

  onEstimationCompleted(breakdown: HourBreakdown): void {
    const estimationId = this.estimationId();
    const versionNumber = this.analysisSummary()?.versionNumber ?? 1;
    const fiability = this.estimationViewModel()?.fiability ?? 0;

    if (!estimationId) {
      return;
    }

    this.isValidatingEstimation.set(true);
    this.estimationValidateError.set(null);

    this.analysisService
      .validateEstimation(estimationId, versionNumber, breakdown, fiability)
      .pipe(
        finalize(() => this.isValidatingEstimation.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (result) => {
          this.estimationId.set(result.id);
          this.approvedBreakdown.set(result.breakdown);
          this.estimationViewModel.update((current) =>
            current
              ? {
                  ...current,
                  fiability: result.fiability,
                  breakdown: result.breakdown,
                  estimationId: result.id,
                  similarRequestsCount: result.similarRequestsCount,
                }
              : current
          );
          this.completeStep('estimation');
        },
        error: (err: unknown) => {
          this.estimationValidateError.set(
            getApiErrorMessage(err, 'No se pudo validar la estimación.')
          );
        },
      });
  }

  onDownloadAnalysis(): void {
    const analysisId = this.analysisId();
    if (!analysisId) {
      return;
    }

    this.isDownloadingAnalysis.set(true);
    this.analysisService
      .triggerAnalysisDownload(analysisId, this.analysisExportFilename())
      .pipe(
        finalize(() => this.isDownloadingAnalysis.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => this.analysisDownloaded.set(true),
        error: () => this.analysisDownloaded.set(false),
      });
  }

  onDownloadPlanning(): void {
    const estimationId = this.estimationId();
    if (!estimationId) {
      return;
    }

    this.isDownloadingPlanning.set(true);
    this.analysisService
      .triggerPlanningDownload(estimationId, this.estimationExportFilename())
      .pipe(
        finalize(() => this.isDownloadingPlanning.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => this.planningDownloaded.set(true),
        error: () => this.planningDownloaded.set(false),
      });
  }

  private initializeRequest(requestId: string): void {
    this.isInitializing.set(true);
    this.analysisSubmitError.set(null);
    this.resetWorkflow();

    this.analysisService
      .lockRequest(requestId)
      .pipe(
        switchMap((request) => {
          this.originRequestCode.set(request.originRequestCode?.trim() || null);
          return this.analysisService.findImpactAnalysisByRequestId(requestId);
        }),
        switchMap((existing) => {
          if (!existing) {
            return of({ existing: null, components: [] as ComponentAnalysisResponse[], estimation: null });
          }

          return forkJoin({
            components: this.analysisService.getComponentAnalysesByAnalysisId(existing.id),
            estimation: this.analysisService.findEstimationByAnalysisId(existing.id),
          }).pipe(
            switchMap(({ components, estimation }) =>
              of({ existing, components, estimation })
            )
          );
        }),
        finalize(() => this.isInitializing.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: ({ existing, components, estimation }) => {
          if (!existing) {
            return;
          }

          this.restoreExistingAnalysis(existing, components, estimation);
        },
        error: (err: unknown) => {
          this.analysisSubmitError.set(
            getApiErrorMessage(err, 'No se pudo preparar el análisis de la petición.')
          );
        },
      });
  }

  private restoreExistingAnalysis(
    existing: ImpactAnalysisResult,
    components: ComponentAnalysisResponse[],
    estimation: EstimationResult | null
  ): void {
    this.analysisId.set(existing.id);
    this.analysisSummary.set(this.toSummary(existing));
    this.completeStep('analysis');

    if (components.length > 0) {
      this.savedComponentAnalyses.set(components);
      this.onComponentSelectionChange(components.map((item) => item.componentId));
      this.completeStep('components');
    }

    if (estimation) {
      this.estimationId.set(estimation.id);
      this.estimationViewModel.set({
        fiability: estimation.fiability,
        suggested: estimation.suggested,
        breakdown: estimation.breakdown,
        estimationId: estimation.id,
        similarRequestsCount: estimation.similarRequestsCount,
      });
      this.approvedBreakdown.set(estimation.breakdown);
      this.completeStep('estimation');
    }
  }

  private resetWorkflow(): void {
    this.stepStates.set({ ...INITIAL_STEP_STATES });
    this.originRequestCode.set(null);
    this.analysisId.set(null);
    this.estimationId.set(null);
    this.analysisSummary.set(null);
    this.savedComponentAnalyses.set([]);
    this.selectedComponentIds.set([]);
    this.estimationViewModel.set(null);
    this.approvedBreakdown.set(null);
    this.analysisDownloaded.set(false);
    this.planningDownloaded.set(false);
    this.componentsLoadError.set(null);
    this.estimationLoadError.set(null);
    this.estimationValidateError.set(null);
    this.componentsLoaded.set(false);
    this.estimationLoadAttempted.set(false);
    this.architecturalComponents.set([]);
  }

  private toSummary(result: ImpactAnalysisResult): ImpactAnalysisDocumentData {
    const documentData = result.documentData;

    return {
      descripcionAbreviada: this.readDocumentField(documentData, 'descripcionAbreviada', 'title'),
      descripcionImpacto: this.readDocumentField(documentData, 'descripcionImpacto', 'notes'),
      descripcionSolucion: this.readDocumentField(documentData, 'descripcionSolucion'),
      requisitosFuncionales: this.readDocumentField(documentData, 'requisitosFuncionales'),
      pruebas: this.readDocumentField(documentData, 'pruebas'),
      complexity: result.complexity,
      versionNumber: result.versionNumber,
    };
  }

  private readDocumentField(
    documentData: Record<string, unknown>,
    primaryKey: string,
    fallbackKey?: string
  ): string {
    const primary = documentData[primaryKey];
    if (primary !== undefined && primary !== null) {
      return String(primary);
    }

    if (fallbackKey) {
      const fallback = documentData[fallbackKey];
      if (fallback !== undefined && fallback !== null) {
        return String(fallback);
      }
    }

    return '';
  }

  private loadArchitecturalComponents(): void {
    if (this.componentsLoaded() || this.isLoadingComponents()) {
      return;
    }

    this.isLoadingComponents.set(true);
    this.componentsLoadError.set(null);

    this.analysisService
      .getArchitecturalComponents()
      .pipe(
        finalize(() => {
          this.isLoadingComponents.set(false);
          this.componentsLoaded.set(true);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (components) => this.architecturalComponents.set(components),
        error: (err: unknown) => {
          this.componentsLoadError.set(
            getApiErrorMessage(err, 'No se pudieron cargar los componentes.')
          );
        },
      });
  }

  private loadEstimation(analysisId: string): void {
    if (this.estimationLoadAttempted() || this.isLoadingEstimation()) {
      return;
    }

    const versionNumber = this.analysisSummary()?.versionNumber ?? 1;

    this.estimationLoadAttempted.set(true);
    this.isLoadingEstimation.set(true);
    this.estimationLoadError.set(null);

    this.analysisService
      .getEstimation(analysisId, versionNumber)
      .pipe(
        finalize(() => this.isLoadingEstimation.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (result) => {
          this.estimationId.set(result.id);
          this.estimationViewModel.set({
            fiability: result.fiability,
            suggested: result.suggested,
            breakdown: result.breakdown,
            estimationId: result.id,
            similarRequestsCount: result.similarRequestsCount,
          });
        },
        error: (err: unknown) => {
          this.estimationLoadError.set(
            getApiErrorMessage(err, 'No se pudo cargar la estimación.')
          );
        },
      });
  }

  private analysisExportFilename(): string {
    const code = this.originRequestCode();
    if (code) {
      return `ImpactAnalysis-${code}.docx`;
    }

    const analysisId = this.analysisId();
    return analysisId ? `ImpactAnalysis-${analysisId}.docx` : 'ImpactAnalysis.docx';
  }

  private estimationExportFilename(): string {
    const code = this.originRequestCode();
    if (code) {
      return `Estimation-${code}.xlsx`;
    }

    const estimationId = this.estimationId();
    return estimationId ? `Estimation-${estimationId}.xlsx` : 'Estimation.xlsx';
  }
}
