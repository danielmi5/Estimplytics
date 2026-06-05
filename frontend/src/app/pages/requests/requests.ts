import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, effect, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { debounceTime, distinctUntilChanged, finalize, switchMap, take, timer } from 'rxjs';
import { Sidebar } from '../../components/layout/sidebar/sidebar';
import { FormInput } from '../../components/shared/form-input/form-input';
import { DataTable } from '../../components/shared/data-table/data-table';
import { Pagination } from '../../components/shared/pagination/pagination';
import { Badge } from '../../components/shared/badge/badge';
import { Button } from '../../components/shared/button/button';
import { Modal } from '../../components/shared/modal/modal';
import { RequestForm } from '../../components/requests/request-form/request-form';
import { RequestsStateService } from '../../core/requests/requests-state.service';
import { RequestResponse } from '../../core/requests/request.dto';
import { RedmineConfigService } from '../../core/configuration/redmine-config.service';
import { NotificationService } from '../../services/notification.service';

const SYNC_RELOAD_DELAY_MS = 100;
const SYNC_RECOVERY_POLL_MS = 2000;
const SYNC_RECOVERY_MAX_ATTEMPTS = 90;

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
    Modal,
    RequestForm,
    DatePipe
  ],
  providers: [RequestsStateService],
  templateUrl: './requests.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RequestsPage implements OnInit {
  private readonly state = inject(RequestsStateService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly redmineConfig = inject(RedmineConfigService);
  private readonly notificationService = inject(NotificationService);

  readonly searchControl = new FormControl('', { nonNullable: true });
  readonly isCreateModalOpen = signal(false);
  readonly requestForm = viewChild(RequestForm);

  readonly items = this.state.items;
  readonly totalElements = this.state.totalElements;
  readonly totalPages = this.state.totalPages;
  readonly page = this.state.page;
  readonly pageSize = this.state.pageSize;
  readonly isLoading = this.state.isLoading;
  readonly isSyncing = computed(
    () => this.redmineConfig.isSyncing() || this.redmineConfig.hasPendingSync()
  );

  readonly canSyncRedmine = computed(
    () => this.redmineConfig.primaryCredential() !== null
  );

  constructor() {
    effect(() => {
      if (!this.state.isCreated()) {
        return;
      }

      this.requestForm()?.resetForm();
      this.closeCreateModal();
      this.state.isCreated.set(false);
    });
  }

  ngOnInit(): void {
    this.redmineConfig
      .refreshCredentials()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe();

    this.reloadRequests();

    if (this.redmineConfig.shouldRecoverSync()) {
      this.notificationService.info('Sincronizando peticiones desde Redmine…');
      this.recoverPendingSync();
    }

    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((search) => {
        this.state.load({ page: 0, size: this.pageSize(), search: search || undefined });
      });
  }

  onPageChange(page: number): void {
    this.reloadRequests(page);
  }

  onSyncRedmine(): void {
    const credential = this.redmineConfig.primaryCredential();
    if (!credential) {
      this.notificationService.warning('Configura las credenciales de Redmine en Configuración.');
      return;
    }

    this.notificationService.info('Sincronizando peticiones desde Redmine…');

    this.redmineConfig
      .syncIssues(credential.id, true, true)
      .pipe(
        switchMap((message) => {
          this.notifySyncResult(message);
          this.notificationService.info('Se están cargando peticiones…');
          return timer(SYNC_RELOAD_DELAY_MS);
        })
      )
      .subscribe({
        next: () => this.reloadRequests(0),
        error: () => {
          this.notificationService.error('No se pudieron sincronizar las peticiones. Inténtalo de nuevo.');
        },
      });
  }

  openCreateModal(): void {
    this.state.isCreated.set(false);
    this.isCreateModalOpen.set(true);
  }

  closeCreateModal(): void {
    this.isCreateModalOpen.set(false);
  }

  demandTone(request: RequestResponse): string {
    return (request.demandType ?? 'default').toLowerCase();
  }

  private recoverPendingSync(): void {
    timer(SYNC_RELOAD_DELAY_MS, SYNC_RECOVERY_POLL_MS)
      .pipe(
        take(SYNC_RECOVERY_MAX_ATTEMPTS),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.redmineConfig.clearSyncState();
          this.notificationService.info('Lista de peticiones actualizada.');
        })
      )
      .subscribe(() => this.reloadRequests(0));
  }

  private reloadRequests(page = this.page()): void {
    this.state.load({
      page,
      size: this.pageSize(),
      search: this.searchControl.value || undefined
    });
  }

  private notifySyncResult(message: string): void {
    const count = this.parseSyncedCount(message);
    if (count === 0) {
      this.notificationService.info('No hay peticiones nuevas en Redmine.');
      return;
    }

    const label = count === 1 ? 'petición' : 'peticiones';
    this.notificationService.success(`Se han sincronizado ${count} ${label} desde Redmine.`);
  }

  private parseSyncedCount(message: string): number {
    const count = Number(message.trim());
    return Number.isFinite(count) ? count : 0;
  }
}
