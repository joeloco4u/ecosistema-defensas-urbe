import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

export type RolReporte = 'TUTOR' | 'JURADO' | 'AMBOS';

export interface FiltroReporte {
  fechaInicio?: string | null;
  fechaFin?: string | null;
  docenteId?: number | null;
  rol?: RolReporte | null;
  escuela?: string | null;
}

export interface TesistaInfo {
  id?: string;
  cedula?: string;
  nombres?: string;
  apellidos?: string;
}

export interface DocenteResumen {
  id?: number;
  nombreCompleto?: string;
}

export interface ProyectoReporte {
  id: string;
  titulo?: string;
  expediente?: string;
  escuela?: string;
  nivelSeminario?: string;
  estatus?: string;
  estudiante?: TesistaInfo | null;
  estudiante2?: TesistaInfo | null;
  estudiante3?: TesistaInfo | null;
  tutor?: DocenteResumen | null;
  tutorMetodologico?: DocenteResumen | null;
}

export interface EspacioInfo {
  id?: string;
  codigoAula?: string;
}

export interface DefensaDTO {
  id: string;
  fecha?: string;
  horaInicio?: string;
  horaFin?: string;
  estatus?: string;
  espacioFisico?: EspacioInfo | null;
  proyecto?: ProyectoReporte | null;
  juradoId?: number | null;
  tutorAcademicoId?: number | null;
  tutorMetodologicoId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class ReporteService {

  private readonly apiUrl = `${environment.apiUrl}/reportes`;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  filtrarReportes(filtro: FiltroReporte): Observable<DefensaDTO[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.post<DefensaDTO[]>(`${this.apiUrl}/filtrar`, filtro, { headers });
  }
}
