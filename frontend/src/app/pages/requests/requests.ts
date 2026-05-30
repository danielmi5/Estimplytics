import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { debounceTime, distinctUntilChanged, startWith } from 'rxjs';
import { Sidebar } from '../../components/layout/sidebar/sidebar';
import { FormInput } from '../../components/shared/form-input/form-input';
import { DataTable } from '../../components/shared/data-table/data-table';
import { Pagination } from '../../components/shared/pagination/pagination';
import { Badge } from '../../components/shared/badge/badge';
import { Button } from '../../components/shared/button/button';
import { RequestsStateService } from '../../core/requests/requests-state.service';
import { RequestResponse } from '../../core/requests/request.dto';

@Component({
  selector: 'app-requests-page',
  standalone: true,
  imports: [
    Sidebar,
    FormInput,
    ReactiveFormsModule,
    DataTable,
    Pagination,
    Badge,
    Button,
    DatePipe
  ],
  providers: [RequestsStateService],
  templateUrl: './requests.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RequestsPage implements OnInit {
  private readonly state = inject(RequestsStateService);
  private readonly destroyRef = inject(DestroyRef);

  readonly searchControl = new FormControl('', { nonNullable: true });

  readonly items = this.state.items;
  readonly totalElements = this.state.totalElements;
  readonly totalPages = this.state.totalPages;
  readonly page = this.state.page;
  readonly pageSize = this.state.pageSize;
  readonly isLoading = this.state.isLoading;

  ngOnInit(): void {
    this.state.load({ page: 0, size: this.pageSize() });
    this.searchControl.valueChanges
      .pipe(
        startWith(this.searchControl.value),
        debounceTime(300),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((search) => {
        this.state.load({ page: 0, size: this.pageSize(), search: search || undefined });
      });
  }

  onPageChange(page: number): void {
    this.state.load({
      page,
      size: this.pageSize(),
      search: this.searchControl.value || undefined
    });
  }

  demandTone(request: RequestResponse): string {
    return (request.demandType ?? 'default').toLowerCase();
  }
}
