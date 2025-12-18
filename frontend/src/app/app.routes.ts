import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';


export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent }, // Matches routerLink="/register"
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'account/reset-password', component: LoginComponent }, // Placeholder for reset-password
  { path: 'chat', loadComponent: () => import('./chat/chat.component').then(m => m.ChatComponent) } // Placeholder
];
