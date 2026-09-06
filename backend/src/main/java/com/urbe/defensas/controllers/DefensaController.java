package com.urbe.defensas.controllers;

import com.urbe.defensas.dtos.RegistroDefensaDTO;
import com.urbe.defensas.models.Defensa;
import com.urbe.defensas.services.DefensaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/defensas")
public class DefensaController {

    private final DefensaService defensaService;

    public DefensaController(DefensaService defensaService) {
        this.defensaService = defensaService;
    }

    @GetMapping
    public ResponseEntity<List<Defensa>> listar(
            @RequestParam(required = false) Long tutorId,
            @RequestParam(required = false) UUID proyectoId,
            @RequestParam(required = false) String escuela) {
        return ResponseEntity.ok(defensaService.listarConFiltros(tutorId, proyectoId, escuela));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Defensa> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(defensaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Defensa> crear(@Valid @RequestBody RegistroDefensaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defensaService.programar(dto));
    }

    @PutMapping("/{id}/reprogramar")
    public ResponseEntity<Defensa> reprogramar(@PathVariable UUID id, @Valid @RequestBody Defensa defensa) {
        return ResponseEntity.ok(defensaService.reprogramar(id, defensa));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<Defensa> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(defensaService.confirmar(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        defensaService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
