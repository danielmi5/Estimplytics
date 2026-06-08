import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Button } from '../../shared/button/button';
import { Badge } from '../../shared/badge/badge';
import { DataTable } from '../../shared/data-table/data-table';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';
import { RequestsApiService } from '../../../core/requests/requests-api.service';
import { RequestResponse } from '../../../core/requests/request.dto';
import { priorityTone } from '../../../core/requests/priority.util';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-requests',
  imports: [TitleCasePipe, DatePipe, RouterLink, Button, Badge, DataTable, FeatherIconDirective],
  templateUrl: './requests.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Requests {
  private readonly api = inject(RequestsApiService);
  private readonly auth = inject(AuthService);

  readonly requests = signal<RequestResponse[]>([]);
  readonly loadError = signal(false);

  constructor() {
    if (!this.auth.isAuthenticated()) {
      return;
    }

    this.api.getAll({ page: 0, size: 50, sort: 'createdDate,desc' }).subscribe({
      next: (page) => {
        const pending = page.content
          .filter((request) => request.status?.toUpperCase() !== 'CLOSED')
          .slice(0, 5);
        this.requests.set(pending);
        this.loadError.set(false);
      },
      error: () => {
        this.requests.set([]);
        this.loadError.set(true);
      }
    });
  }

  sourceLabel(request: RequestResponse): string {
    return request.redmineId != null || request.originRequestCode?.startsWith('REDMINE-') ? 'Redmine' : 'Manual';
  }

  requestPriorityTone(request: RequestResponse): string {
    return priorityTone(request.priority);
  }
}
