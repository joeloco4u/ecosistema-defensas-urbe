package com.urbe.defensas.controllers;

import com.urbe.defensas.models.EspacioFisico;
import com.urbe.defensas.services.EspacioFisicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/espacios")
public class EspacioFisicoController {

    private final EspacioFisicoService espacioFisicoService;

    public EspacioFisicoController(EspacioFisicoService espacioFisicoService) {
        this.espacioFisicoService = espacioFisicoService;
    }

    @GetMapping
    public ResponseEntity<List<EspacioFisico>> listar() {
        return ResponseEntity.ok(espacioFisicoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspacioFisico> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(espacioFisicoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<EspacioFisico> crear(@Valid @RequestBody EspacioFisico espacio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(espacioFisicoService.crear(espacio));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspacioFisico> actualizar(@PathVariable UUID id, @Valid @RequestBody EspacioFisico espacio) {
        return ResponseEntity.ok(espacioFisicoService.actualizar(id, espacio));
    }
}
