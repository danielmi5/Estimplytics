import { ChangeDetectionStrategy, Component, OnInit, effect, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { FormInput } from '../../shared/form-input/form-input';
import { Button } from '../../shared/button/button';
import { RedmineConfigService } from '../../../core/configuration/redmine-config.service';

@Component({
  selector: 'app-redmine-sync-form',
  standalone: true,
  imports: [ReactiveFormsModule, FormInput, Button],
  templateUrl: './redmine-sync-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RedmineSyncForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  readonly redmineService = inject(RedmineConfigService);

  readonly form = this.fb.group({
    redmineUrl: [''],
    apiKey:     [''],
  });

  constructor() {
    effect(() => {
      const cred = this.redmineService.primaryCredential();
      if (cred && !this.urlCtrl.dirty) {
        this.form.patchValue({ redmineUrl: cred.instanceName });
      }
    });
  }

  get urlCtrl()    { return this.form.controls.redmineUrl; }
  get apiKeyCtrl() { return this.form.controls.apiKey; }

  ngOnInit(): void {
    this.redmineService.loadCredentials();
  }

  onSubmit(): void {
    const { redmineUrl, apiKey } = this.form.getRawValue();

    if (!redmineUrl.trim()) {
      const cred = this.redmineService.primaryCredential();
      if (cred) {
        this.redmineService.deleteCredential(cred.id);
      }

      this.form.patchValue({ redmineUrl: '', apiKey: '' });
      this.form.markAsPristine();
      return;
    }

    const request: { redmineUrl: string; plainApiKey?: string } = {
      redmineUrl: redmineUrl.trim(),
    };

    if (this.apiKeyCtrl.dirty) {
      request.plainApiKey = apiKey.trim();
    }

    this.form.markAsPristine();
    this.redmineService.save(request);
  }
}
