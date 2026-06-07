import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormInput } from '../../shared/form-input/form-input';
import { Button } from '../../shared/button/button';
import { UserProfileService } from '../../../services/user-profile.service';
import { UserUpdate } from '../../../core/users/user.dto';
import { emailTldValidator, getProfileFieldMessage, getProfileFieldState, passwordStrengthValidator, type ProfileFieldName, type RegisterFieldState } from '../../../form/validators';

@Component({
  selector: 'app-user-profile-form',
  standalone: true,
  imports: [ReactiveFormsModule, FormInput, Button],
  templateUrl: './user-profile-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserProfileForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly profileService = inject(UserProfileService);

  readonly form = this.fb.group({
    name: ['', [Validators.minLength(2), Validators.maxLength(100)]],
    email: ['', [emailTldValidator()]],
    password: ['', [Validators.minLength(8), passwordStrengthValidator()]],
  });

  ngOnInit(): void {
    const stored = this.profileService.getStoredUser();
    if (stored) {
      this.form.patchValue({
        name: stored.name ?? '',
        email: stored.email,
      });
    }
  }

  fieldState(fieldName: ProfileFieldName): RegisterFieldState {
    return getProfileFieldState(this.form.get(fieldName));
  }

  fieldMessage(fieldName: ProfileFieldName, state: RegisterFieldState): string {
    return getProfileFieldMessage(fieldName, this.form.get(fieldName), state);
  }

  onSubmit(): void {
    this.form.markAllAsTouched();

    if (this.form.invalid) {
      return;
    }

    const { name, email, password } = this.form.getRawValue();
    const update: UserUpdate = {};

    const nameValue = (name ?? '').trim();
    const emailValue = (email ?? '').trim();
    const passwordValue = (password ?? '').trim();
    
    let hasValue = false;

    if (nameValue) {
      update.name = nameValue;

      hasValue = true;
    }
    if (emailValue) {
      update.email = emailValue;

      hasValue = true;
    }
    if (passwordValue) {
      update.password = passwordValue;

      hasValue = true;
    }

    if (!hasValue) return;

    this.profileService.updateProfile(update);
  }
}
