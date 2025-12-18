import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common'; // For *ngIf
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../Auth.service';
import { Router } from '@angular/router';
import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap'; // For ngb-alert
import { CarouselModule } from 'ngx-owl-carousel-o'; // For owl-carousel-o
import { OwlOptions } from 'ngx-owl-carousel-o'; // Import OwlOptions

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgbAlertModule,CarouselModule], // Required imports
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  signupForm!: FormGroup;
  submitted = false;
  successmsg = false;
  error: string | null = null;
  year = new Date().getFullYear();

  // Carousel options from reference
  carouselOption: OwlOptions = {
    items: 1,
    loop: false,
    margin: 0,
    nav: false,
    dots: true,
    responsive: {
      680: { items: 1 }
    }
  };

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    document.body.classList.add('auth-body-bg'); // Match reference styling
    this.signupForm = this.fb.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  // Convenience getter for form fields
  get f() { return this.signupForm.controls; }

  onSubmit() {
    this.submitted = true;

    if (this.signupForm.invalid) {
      return;
    }

    const { username, email, password } = this.signupForm.value;
    this.authService.register(email, username, password).subscribe({
      next: (response) => {
        this.successmsg = true;
        this.error = null;
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.error = err.message || 'Registration failed';
        this.successmsg = false;
      }
    });
  }
}