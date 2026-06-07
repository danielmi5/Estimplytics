import { Component, ChangeDetectionStrategy } from '@angular/core';
import { Button } from '../../shared/button/button';

@Component({
  selector: 'app-welcome',
  imports: [Button],
  templateUrl: './welcome.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Welcome {}
