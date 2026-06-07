import { AbstractControl } from '@angular/forms';

export type RegisterFieldName = 'name' | 'email' | 'password' | 'confirmPassword' | 'acceptTerms';
export type ProfileFieldName = 'name' | 'email' | 'password';
export type RegisterFieldState = 'initial' | 'warning' | 'error' | 'success';

const messages = {
	warning: {
		name: 'El nombre es obligatorio',
		email: 'El correo electrónico es obligatorio',
		password: 'La contraseña es obligatoria',
		confirmPassword: 'La confirmación de contraseña es obligatoria',
		acceptTerms: 'Debes aceptar los términos'
	},
	error: {
		name: 'El nombre debe tener al menos 2 caracteres',
		email: 'El correo electrónico no tiene un formato válido',
		emailTaken: 'Este correo electrónico ya está registrado',
		passwordLength: 'La contraseña debe tener al menos 8 caracteres',
		passwordLetter: 'La contraseña debe contener al menos una letra',
		passwordNumber: 'La contraseña debe contener al menos un número',
		passwordSymbol: 'La contraseña debe contener al menos un símbolo',
		passwordMismatch: 'Las contraseñas no coinciden',
		confirmPassword: 'La confirmación de contraseña no es válida',
		acceptTerms: 'Debes aceptar los términos y la política de privacidad'
	},
	success: {
		name: 'El nombre es correcto',
		email: 'El correo electrónico es correcto',
		password: 'La contraseña es correcta',
		confirmPassword: 'La confirmación de contraseña es correcta',
		acceptTerms: 'Los términos han sido aceptados'
	}
} as const;

const touchedOrDirty = (control: AbstractControl, force = false): boolean => force || control.touched || control.dirty;

export function getRegisterFieldState(control: AbstractControl | null, force = false): RegisterFieldState {
	if (!control || !touchedOrDirty(control, force)) {
		return 'initial';
	}

	if (control.pending) {
		return 'initial';
	}

	if (control.valid) {
		return 'success';
	}

	if (control.errors?.['required'] && !control.value) {
		return 'warning';
	}

	return 'error';
}

export function getRegisterFieldMessage(fieldName: RegisterFieldName, control: AbstractControl | null, state: RegisterFieldState): string {
	if (!control) {
		return '';
	}

	if (state === 'warning') {
		return messages.warning[fieldName];
	}

	if (state === 'success') {
		return messages.success[fieldName] ?? '';
	}

	if (fieldName === 'email') {
		if (control.errors?.['emailTaken']) {
			return messages.error.emailTaken;
		}

		if (control.errors?.['email']) {
			return messages.error.email;
		}
	}

	if (fieldName === 'name' && control.errors?.['minlength']) {
		return messages.error.name;
	}

	if (fieldName === 'password') {
		const strength = control.errors?.['passwordStrength'];

		if (strength) {
			if (!strength.hasMinimumLength) return messages.error.passwordLength;
			if (!strength.hasLetter) return messages.error.passwordLetter;
			if (!strength.hasNumber) return messages.error.passwordNumber;
			if (!strength.hasSymbol) return messages.error.passwordSymbol;
		}
	}

	if (fieldName === 'confirmPassword' && control.errors?.['passwordMismatch']) {
		return messages.error.passwordMismatch;
	}

	if (fieldName === 'acceptTerms' && control.errors?.['required']) {
		return messages.error.acceptTerms;
	}

	if (control.errors?.['minlength']) {
		return messages.error.name;
	}

	return '';
}

export function getRegisterSubmitLabel(isPending: boolean): string {
	return isPending ? 'Validando...' : 'Registrarse';
}

const hasValue = (control: AbstractControl): boolean => {
	const value = control.value;
	return typeof value === 'string' ? value.trim().length > 0 : Boolean(value);
};

export function getProfileFieldState(control: AbstractControl | null, force = false): RegisterFieldState {
	if (!control || !touchedOrDirty(control, force)) {
		return 'initial';
	}

	if (control.pending) {
		return 'initial';
	}

	if (!hasValue(control)) {
		return 'initial';
	}

	if (control.valid) {
		return 'success';
	}

	return 'error';
}

export function getProfileFieldMessage(
	fieldName: ProfileFieldName,
	control: AbstractControl | null,
	state: RegisterFieldState
): string {
	return getRegisterFieldMessage(fieldName, control, state);
}