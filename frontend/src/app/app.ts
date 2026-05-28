import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Footer } from './components/layout/footer/footer';
import { Header } from './components/layout/header/header';
import { NotificationList } from './components/shared/notification/notification';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [Footer, Header, RouterOutlet, NotificationList],
  templateUrl: './app.html'
})
export class App {}
