import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ReporteService, DefensaDTO, FiltroReporte } from '../../../core/services/reporte.service';

@Component({
  selector: 'app-pre-defensa-print',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pre-defensa-print.component.html',
})
export class PreDefensaPrintComponent implements OnInit {
  escuela = '';
  defensas: DefensaDTO[] = [];
  cargando = true;

  constructor(
    private route: ActivatedRoute,
    private reporteService: ReporteService,
  ) {}

  ngOnInit(): void {
    this.escuela = decodeURIComponent(this.route.snapshot.paramMap.get('escuela') ?? '');
    this.cargar();
  }

  private cargar(): void {
    const filtro: FiltroReporte =
      this.escuela && this.escuela !== 'Todas' ? { escuela: this.escuela } : {};

    this.reporteService.filtrarReportes(filtro).subscribe({
      next: (defensas) => {
        this.defensas = defensas ?? [];
        this.cargando = false;
        setTimeout(() => this.imprimirEnVentanaLimpia(), 500);
      },
      error: (err) => {
        console.error('Error al cargar las defensas para la planilla', err);
        this.cargando = false;
      },
    });
  }

  private imprimirEnVentanaLimpia(): void {
    const printContents = document.querySelector('.bg-white')?.innerHTML;
    if (!printContents) return;

    const html = this.construirDocumento(printContents);
    const printWindow = window.open('', '_blank', 'width=1000,height=800');

    if (printWindow) {
      printWindow.document.write(html);
      printWindow.document.close();
      printWindow.focus();
      setTimeout(() => {
        printWindow.print();
        printWindow.close();
      }, 250);
      return;
    }

    this.imprimirEnIframe(html);
  }

  private imprimirEnIframe(html: string): void {
    const iframe = document.createElement('iframe');
    iframe.setAttribute('aria-hidden', 'true');
    iframe.style.cssText = 'position:fixed;right:0;bottom:0;width:0;height:0;border:0;';
    document.body.appendChild(iframe);

    const doc = iframe.contentWindow?.document;
    if (!doc) {
      iframe.remove();
      return;
    }

    doc.open();
    doc.write(html);
    doc.close();

    setTimeout(() => {
      iframe.contentWindow?.focus();
      iframe.contentWindow?.print();
      setTimeout(() => iframe.remove(), 1000);
    }, 250);
  }

  private construirDocumento(contenido: string): string {
    return `
      <html>
        <head>
          <title>Pre-Defensas</title>
          <style>
            @page { size: landscape; margin: 10mm; }
            body { font-family: 'Times New Roman', Times, serif; color: #000; background: #fff; padding: 20px; }
            table { width: 100%; border-collapse: collapse; table-layout: fixed; font-size: 10px; }
            th, td { border: 1px solid #000; padding: 4px; vertical-align: top; word-wrap: break-word; }
            th { background-color: #e5e7eb; font-size: 9px; text-transform: uppercase; }
            .predefensa-grupo { width: 22%; }
            .predefensa-exp { width: 8%; }
            .predefensa-titulo { width: 22%; }
            .predefensa-check { width: 20px; }
            .text-justify { text-align: justify; }
            .whitespace-normal { white-space: normal; }
            .text-xs { font-size: 10px; }
            .text-center { text-align: center; }
            .font-bold { font-weight: bold; }
            .uppercase { text-transform: uppercase; }
            .mb-4 { margin-bottom: 16px; }
            .mb-3 { margin-bottom: 12px; }
            .mt-6 { margin-top: 24px; }
            .block { display: block; }
            .mt-1 { margin-top: 4px; }
            .flex { display: flex; }
            .justify-between { justify-content: space-between; }
          </style>
        </head>
        <body>
          ${contenido}
        </body>
      </html>
    `;
  }

  tesistas(d: DefensaDTO): string {
    const p = d.proyecto;
    const nombres = [p?.estudiante, p?.estudiante2, p?.estudiante3]
      .map((e) => [e?.nombres, e?.apellidos].filter(Boolean).join(' ').trim())
      .filter((nombre) => nombre.length > 0);
    return nombres.length > 0 ? nombres.join(', ') : 'Por asignar';
  }

  tutorAcademico(d: DefensaDTO): string {
    return d.proyecto?.tutor?.nombreCompleto || 'Por asignar';
  }
}
