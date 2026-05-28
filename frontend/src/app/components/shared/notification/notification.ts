import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NotificationService } from '../../../services/notification.service';
import { FeatherIconDirective } from "@app/directives/feather-icon.directive";

@Component({
  selector: 'app-notification',
  standalone: true,
  templateUrl: './notification.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FeatherIconDirective],
})
export class NotificationList {
  protected readonly notificationService = inject(NotificationService);
}
