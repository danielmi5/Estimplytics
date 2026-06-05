import { Component, ChangeDetectionStrategy, computed, input, output } from '@angular/core';
import { Button } from '../button/button';
import { buildPaginationItems } from './pagination-items.util';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [Button],
  templateUrl: './pagination.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Pagination {
  readonly page = input.required<number>();
  readonly pageSize = input.required<number>();
  readonly totalElements = input.required<number>();
  readonly totalPages = input.required<number>();

  readonly pageChange = output<number>();

  readonly showingCount = computed(() => {
    if (this.totalElements() === 0) {
      return 0;
    }
    const start = this.page() * this.pageSize() + 1;
    const end = Math.min((this.page() + 1) * this.pageSize(), this.totalElements());
    return end - start + 1;
  });

  readonly pageItems = computed(() => buildPaginationItems(this.page(), this.totalPages()));

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages() || page === this.page()) {
      return;
    }
    this.pageChange.emit(page);
  }
}
