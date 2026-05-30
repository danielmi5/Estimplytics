import { Routes } from '@angular/router';
import { authGuard, publicGuard } from './guards';

export const routes: Routes = [
	{
		path: '',
		loadComponent: () => import('./pages/home/home').then((m) => m.HomePage),
		title: 'Inicio'
	},
	{
		path: 'register',
		loadComponent: () => import('./pages/register/register').then((m) => m.RegisterPage),
		canActivate: [publicGuard],
		title: 'Registro'
	},
	{
		path: 'login',
		loadComponent: () => import('./pages/login/login').then((m) => m.LoginPage),
		canActivate: [publicGuard],
		title: 'Login'
	},
	{
		path: 'configuration',
		loadComponent: () => import('./pages/configuration/configuration').then((m) => m.ConfigurationPage),
		canActivate: [authGuard],
		title: 'Configuración'
	},
	{
		path: 'requests',
		loadComponent: () => import('./pages/requests/requests').then((m) => m.RequestsPage),
		canActivate: [authGuard],
		title: 'Peticiones'
	},
	{
		path: 'home',
		redirectTo: '',
		pathMatch: 'full'
	},
	{
		path: '**',
		redirectTo: ''
	}
];
