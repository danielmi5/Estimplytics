import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { Button } from '../../shared/button/button';
import { FormInput } from '../../shared/form-input/form-input';
import { emailTldValidator, getRegisterFieldMessage, getRegisterFieldState, type RegisterFieldName, type RegisterFieldState } from '../../../form/validators';
import { AppStateService, AuthService } from '../../../services';
import { NotificationService } from '../../../services/notification.service';

@Component({
	selector: 'app-login-form',
	standalone: true,
	imports: [ReactiveFormsModule, RouterLink, Button, FormInput],
	templateUrl: './login-form.html',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginForm {
	private readonly fb = inject(FormBuilder);
	private readonly auth = inject(AuthService);
	private readonly appState = inject(AppStateService);
	private readonly notificationService = inject(NotificationService);
	private readonly router = inject(Router);
	private readonly route = inject(ActivatedRoute);

	readonly registerSuccess = toSignal(
		this.route.queryParamMap.pipe(map((params) => params.get('registered') === 'true')),
		{ initialValue: false }
	);

	readonly returnUrl = toSignal(
		this.route.queryParamMap.pipe(map((params) => params.get('returnUrl'))),
		{ initialValue: null }
	);

	readonly form = this.fb.nonNullable.group({
		email: ['', [Validators.required, emailTldValidator()]],
		password: ['', [Validators.required]]
	});

	constructor() {
		this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
			const email = params.get('email');
			if (email) {
				this.form.patchValue({ email });
			}
		});
	}

	fieldState(fieldName: RegisterFieldName): RegisterFieldState {
		return getRegisterFieldState(this.form.get(fieldName));
	}

	fieldMessage(fieldName: RegisterFieldName, state: RegisterFieldState): string {
		return getRegisterFieldMessage(fieldName, this.form.get(fieldName), state);
	}

	get submitLabel(): string {
		return this.form.pending ? 'Validando...' : 'Iniciar sesión';
	}

	onSubmit(): void {
		this.form.markAllAsTouched();

		if (this.form.invalid) {
			return;
		}

		const { email, password } = this.form.getRawValue();
		this.appState.setLoading(true);

		this.auth.login({ email, password }).subscribe({
			next: () => {
				this.appState.setLoading(false);
				const redirect = this.route.snapshot.queryParamMap.get('returnUrl');
				const safeRedirect = redirect?.startsWith('/') ? redirect : '/';
				void this.router.navigateByUrl(safeRedirect);
			},
			error: () => {
				this.appState.setLoading(false);
				this.notificationService.error('Credenciales inválidas. Verifica tu correo y contraseña.');
			}
		});
	}
}
