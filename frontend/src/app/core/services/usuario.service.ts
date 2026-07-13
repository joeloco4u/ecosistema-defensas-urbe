import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface RegistroRequest {
  email: string;
  password: string;
  nombreCompleto: string;
  rol: string;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly apiUrl = `${environment.apiUrl}/usuarios/registrar`;

  constructor(private http: HttpClient) {}

  registrar(data: RegistroRequest): Observable<any> {
    return this.http.post(this.apiUrl, data);
  }
}
