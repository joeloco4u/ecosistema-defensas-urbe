package com.urbe.defensas.controllers;

import com.urbe.defensas.dtos.DocenteHorarioImportDTO;
import com.urbe.defensas.dtos.ImportacionHorariosResultadoDTO;
import com.urbe.defensas.services.HorarioImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioImportController {

    private final HorarioImportService horarioImportService;

    public HorarioImportController(HorarioImportService horarioImportService) {
        this.horarioImportService = horarioImportService;
    }

    @PostMapping(value = "/importar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportacionHorariosResultadoDTO> importar(
            @RequestBody List<DocenteHorarioImportDTO> docentes) {
        return ResponseEntity.ok(horarioImportService.importar(docentes));
    }
}
