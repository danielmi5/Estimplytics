import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-data-table',
  standalone: true,
  templateUrl: './data-table.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DataTable {}
