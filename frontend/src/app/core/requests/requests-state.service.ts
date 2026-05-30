import { Injectable, inject, signal } from '@angular/core';
import { PageParams } from '../dtos/pagination.dto';
import { RequestRequest, RequestResponse, RequestUpdate } from './request.dto';
import { RequestsApiService } from './requests-api.service';

@Injectable()
export class RequestsStateService {
  private readonly api = inject(RequestsApiService);

  readonly items = signal<RequestResponse[]>([]);
  readonly totalElements = signal<number>(0);
  readonly totalPages = signal<number>(0);
  readonly page = signal<number>(0);
  readonly pageSize = signal<number>(10);
  readonly search = signal<string>('');
  readonly isLoading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  load(params?: PageParams): void {
    const page = params?.page ?? this.page();
    const size = params?.size ?? this.pageSize();
    const search = params?.search ?? this.search();

    this.isLoading.set(true);
    this.error.set(null);
    this.api.getAll({ page, size, search: search || undefined }).subscribe({
      next: (result) => {
        this.items.set(result.content);
        this.totalElements.set(result.totalElements);
        this.totalPages.set(result.totalPages);
        this.page.set(result.number);
        this.pageSize.set(result.size);
        this.search.set(search);
        this.isLoading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(err instanceof Error ? err.message : 'Error loading requests');
        this.isLoading.set(false);
      }
    });
  }

  create(body: RequestRequest): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.api.create(body).subscribe({
      next: () => {
        this.load({ page: this.page(), size: this.pageSize(), search: this.search() || undefined });
      },
      error: (err: unknown) => {
        this.error.set(err instanceof Error ? err.message : 'Error creating request');
        this.isLoading.set(false);
      }
    });
  }

  update(id: string, body: RequestUpdate): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.api.update(id, body).subscribe({
      next: () => {
        this.load({ page: this.page(), size: this.pageSize(), search: this.search() || undefined });
      },
      error: (err: unknown) => {
        this.error.set(err instanceof Error ? err.message : 'Error updating request');
        this.isLoading.set(false);
      }
    });
  }

  delete(id: string): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.api.delete(id).subscribe({
      next: () => {
        this.load({ page: this.page(), size: this.pageSize(), search: this.search() || undefined });
      },
      error: (err: unknown) => {
        this.error.set(err instanceof Error ? err.message : 'Error deleting request');
        this.isLoading.set(false);
      }
    });
  }
}
