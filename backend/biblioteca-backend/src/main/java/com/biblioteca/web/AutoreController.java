package com.biblioteca.web;

import com.biblioteca.dto.AutoreDTO;
import com.biblioteca.dto.AutoreRequest;
import com.biblioteca.service.AutoreService;
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
@RequestMapping("/api/authors")
public class AutoreController {

    private final AutoreService autoreService;

    public AutoreController(AutoreService autoreService) {
        this.autoreService = autoreService;
    }

    @GetMapping("/all")
    public List<AutoreDTO> all() {
        return autoreService.findAll();
    }

    @GetMapping("/find/{id}")
    public AutoreDTO get(@PathVariable Long id) {
        return autoreService.findById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<AutoreDTO> create(@Valid @RequestBody AutoreRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autoreService.create(req));
    }

    @PutMapping("/update/{id}")
    public AutoreDTO update(@PathVariable Long id, @Valid @RequestBody AutoreRequest req) {
        return autoreService.update(id, req);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        autoreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
