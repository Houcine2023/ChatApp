import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  NgModel,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AuthService } from '../Auth.service';
import { Router, RouterLink } from '@angular/router';
import { NgClass, NgIf } from '@angular/common';
import { routes } from '../../app.routes';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [NgClass, ReactiveFormsModule, NgIf,RouterLink], // No need for form modules here due to global provision
  // Removed provideHttpClient() from providers; AuthService is optional here
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  loginForm: FormGroup;
  submitted = false;
  error: string | null = null;
  year = new Date().getFullYear();

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
    });
  }

 onSubmit() {
    this.submitted = true;
    if (this.loginForm.invalid) {
      return;
    }

    const { email, password } = this.loginForm.value;
    this.authService.login(email, password).subscribe({
      next: (response) => {
        localStorage.setItem('userId', response.data!);
        this.authService.sendPresence(response.data!);
        this.router.navigate(['/chat']);
      },
      error: (err) => {
        this.error = err.message;
      }
    });
  }

  get f() { return this.loginForm.controls; }
}
