import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UsuarioService } from '../../../core/services/usuario.service';

@Component({
  selector: 'app-registro-usuario',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './registro-usuario.component.html',
})
export class RegistroUsuarioComponent {
  loading = false;
  exito = false;
  error = '';

  roles = ['COORDINADOR', 'DOCENTE', 'ESTUDIANTE'];

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    nombreCompleto: ['', Validators.required],
    rol: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(4)]],
  });

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
  ) {}

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.error = '';
    this.exito = false;

    this.usuarioService.registrar(this.form.getRawValue()).subscribe({
      next: () => {
        this.exito = true;
        this.loading = false;
        this.form.reset({ rol: '' });
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.mensaje || err.error?.message || 'Error al registrar el usuario';
      },
    });
  }
}
