import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';
import { Button } from '../../shared/button/button';
import type { StepState } from '../../../pages/analysis/step-state';

@Component({
  selector: 'app-step-files',
  imports: [Button, FeatherIconDirective],
  templateUrl: './step-files.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepFiles {
  readonly state = input<StepState>('inactive');
  readonly canDownload = input(false);
  readonly versionDisplay = input('1.0');
  readonly isDownloadingAnalysis = input(false);
  readonly isDownloadingPlanning = input(false);
  readonly analysisDownloaded = input(false);
  readonly planningDownloaded = input(false);

  readonly downloadAnalysis = output<void>();
  readonly downloadPlanning = output<void>();
  readonly stepCompleted = output<void>();
  readonly stepEdit = output<void>();

  onDownloadAnalysis(): void {
    this.downloadAnalysis.emit();
  }

  onDownloadPlanning(): void {
    this.downloadPlanning.emit();
  }

  onComplete(): void {
    this.stepCompleted.emit();
  }

  onEdit(): void {
    this.stepEdit.emit();
  }
}
