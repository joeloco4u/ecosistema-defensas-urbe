import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  loading = false;
  error: string | null = null;

  loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
  ) {}

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.error = null;

    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.loading = false;
        this.error = err?.status === 401
          ? 'Credenciales incorrectas. Verifica tu correo y contraseña.'
          : 'No se pudo iniciar sesión. Intenta nuevamente.';
      },
    });
  }
}
