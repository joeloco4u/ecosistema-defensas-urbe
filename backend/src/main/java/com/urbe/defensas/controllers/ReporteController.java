package com.urbe.defensas.controllers;

import com.urbe.defensas.dtos.DefensaDTO;
import com.urbe.defensas.dtos.FiltroReporteDTO;
import com.urbe.defensas.services.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping("/filtrar")
    public ResponseEntity<List<DefensaDTO>> filtrar(@RequestBody(required = false) FiltroReporteDTO filtro) {
        return ResponseEntity.ok(reporteService.generarReporte(filtro));
    }
}
