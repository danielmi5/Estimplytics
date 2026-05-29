import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Sidebar } from '../../components/layout/sidebar/sidebar';
import { UserProfileForm } from '../../components/configuration/user-profile-form/user-profile-form';
import { RedmineSyncForm } from '../../components/configuration/redmine-sync-form/redmine-sync-form';

@Component({
  selector: 'app-configuration',
  standalone: true,
  imports: [Sidebar, UserProfileForm, RedmineSyncForm],
  templateUrl: './configuration.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfigurationPage {}
