package com.urbe.defensas.controllers;

import com.urbe.defensas.models.TutorSugerido;
import com.urbe.defensas.services.TutorSugeridoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tutores-sugeridos")
public class TutorSugeridoController {

    private final TutorSugeridoService tutorSugeridoService;

    public TutorSugeridoController(TutorSugeridoService tutorSugeridoService) {
        this.tutorSugeridoService = tutorSugeridoService;
    }

    @PostMapping
    public ResponseEntity<TutorSugerido> crear(@Valid @RequestBody TutorSugerido tutor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tutorSugeridoService.crear(tutor));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<TutorSugerido>> listarPendientes() {
        return ResponseEntity.ok(tutorSugeridoService.listarPendientes());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TutorSugerido> cambiarEstado(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");
        return ResponseEntity.ok(tutorSugeridoService.cambiarEstado(id, nuevoEstado));
    }
}
