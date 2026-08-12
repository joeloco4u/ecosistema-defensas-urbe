package com.urbe.defensas.controllers;

import com.urbe.defensas.dtos.SugerenciaHorarioDTO;
import com.urbe.defensas.services.MotorProgramacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/programacion")
public class ProgramacionController {

    private final MotorProgramacionService motorProgramacionService;

    public ProgramacionController(MotorProgramacionService motorProgramacionService) {
        this.motorProgramacionService = motorProgramacionService;
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<List<SugerenciaHorarioDTO>> obtenerSugerencias(
            @RequestParam(name = "cedulas") List<String> cedulas,
            @RequestParam(name = "espacioId") UUID espacioId) {
        List<SugerenciaHorarioDTO> sugerencias = motorProgramacionService.calcularDisponibilidad(cedulas, espacioId);
        return ResponseEntity.ok(sugerencias);
    }
}
