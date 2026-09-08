package com.biblioteca.web;

import com.biblioteca.dto.LibroDTO;
import com.biblioteca.dto.LibroRequest;
import com.biblioteca.service.LibroService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping("/find")
    public List<LibroDTO> find(@RequestParam(required = false) String titolo,
                               @RequestParam(required = false) Long categoriaId,
                               @RequestParam(required = false) String autore) {
        return libroService.search(titolo, categoriaId, autore);
    }

    @GetMapping("/find/{id}")
    public LibroDTO get(@PathVariable Long id) {
        return libroService.findById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<LibroDTO> create(@Valid @RequestBody LibroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroService.create(req));
    }

    @PutMapping("/update/{id}")
    public LibroDTO update(@PathVariable Long id, @Valid @RequestBody LibroRequest req) {
        return libroService.update(id, req);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        libroService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
