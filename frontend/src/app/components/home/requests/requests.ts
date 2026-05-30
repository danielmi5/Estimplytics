import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Button } from '../../shared/button/button';
import { Badge } from '../../shared/badge/badge';
import { DataTable } from '../../shared/data-table/data-table';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';
import { RequestsApiService } from '../../../core/requests/requests-api.service';
import { RequestResponse } from '../../../core/requests/request.dto';

@Component({
  selector: 'app-requests',
  imports: [TitleCasePipe, DatePipe, RouterLink, Button, Badge, DataTable, FeatherIconDirective],
  templateUrl: './requests.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Requests {
  private readonly api = inject(RequestsApiService);

  readonly requests = signal<RequestResponse[]>([]);

  constructor() {
    this.api.getAll({ page: 0, size: 5, sort: 'createdDate,desc' }).subscribe({
      next: (page) => this.requests.set(page.content)
    });
  }

  sourceLabel(request: RequestResponse): string {
    return request.redmineId != null || request.originRequestCode?.startsWith('REDMINE-') ? 'Redmine' : 'Manual';
  }

  priorityTone(request: RequestResponse): string {
    switch (request.priority?.toLowerCase()) {
      case 'high':
      case 'urgent':
        return 'high';
      case 'low':
        return 'low';
      default:
        return 'medium';
    }
  }
}
