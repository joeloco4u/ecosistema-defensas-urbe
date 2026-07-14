import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProgramacionService {

  private readonly apiUrl = 'https://ecosistema-defensas-urbe-production.up.railway.app/api/programacion';

  constructor(private http: HttpClient) {}

  getSugerencias(cedulas: string[], espacioId: string): Observable<any[]> {
    const params = new HttpParams({
      fromObject: {
        espacioId: espacioId,
        cedulas: cedulas.join(',')
      }
    });
    return this.http.get<any[]>(`${this.apiUrl}/sugerencias`, { params });
  }
}
