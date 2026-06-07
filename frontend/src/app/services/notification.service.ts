import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
  id: number;
  type: NotificationType;
  message: string;
  timerId?: ReturnType<typeof setTimeout>;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly _notifications = signal<Notification[]>([]);
  readonly notifications = this._notifications.asReadonly();

  private show(type: NotificationType, message: string): void {
    const isDuplicate = this._notifications().some(n => n.message === message);
    if (isDuplicate) this.notifications().length = 0;

    const id = Date.now();
    const timerId = setTimeout(() => this.remove(id), 5000);
    this._notifications.update(list => [...list, { id, type, message, timerId }]);
  }

  success(message: string): void {
    this.show('success', message);
  }

  error(message: string): void {
    this.show('error', message);
  }

  warning(message: string): void {
    this.show('warning', message);
  }

  info(message: string): void {
    this.show('info', message);
  }

  remove(id: number): void {
    const notification = this._notifications().find(n => n.id === id);
    if (notification?.timerId !== undefined) {
      clearTimeout(notification.timerId);
    }
    this._notifications.update(list => list.filter(n => n.id !== id));
  }
}
