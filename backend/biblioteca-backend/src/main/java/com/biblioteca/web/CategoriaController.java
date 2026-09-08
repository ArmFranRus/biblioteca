package com.biblioteca.web;

import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.dto.CategoriaRequest;
import com.biblioteca.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping("/all")
    public List<CategoriaDTO> all() {
        return categoriaService.findAll();
    }

    @GetMapping("/find/{id}")
    public CategoriaDTO get(@PathVariable Long id) {
        return categoriaService.findById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<CategoriaDTO> create(@Valid @RequestBody CategoriaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.create(req));
    }

    @PutMapping("/update/{id}")
    public CategoriaDTO update(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return categoriaService.update(id, req);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
