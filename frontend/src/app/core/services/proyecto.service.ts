import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProyectoService {

  private readonly apiUrl = `${environment.apiUrl}/proyectos`;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  listarProyectos(): Observable<any[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<any[]>(this.apiUrl, { headers });
  }
}
