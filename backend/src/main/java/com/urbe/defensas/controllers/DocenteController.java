package com.urbe.defensas.controllers;

import com.urbe.defensas.models.Docente;
import com.urbe.defensas.services.DocenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping
    public ResponseEntity<List<Docente>> listar() {
        return ResponseEntity.ok(docenteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Docente> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(docenteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Docente> crear(@Valid @RequestBody Docente docente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteService.crear(docente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Docente> actualizar(@PathVariable Long id, @Valid @RequestBody Docente docente) {
        return ResponseEntity.ok(docenteService.actualizar(id, docente));
    }

}
