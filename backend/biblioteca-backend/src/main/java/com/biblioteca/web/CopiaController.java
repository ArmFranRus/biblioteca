package com.biblioteca.web;

import com.biblioteca.dto.CopiaDTO;
import com.biblioteca.dto.CopiaRequest;
import com.biblioteca.dto.CopiaStatoRequest;
import com.biblioteca.service.CopiaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CopiaController {

    private final CopiaService copiaService;

    public CopiaController(CopiaService copiaService) {
        this.copiaService = copiaService;
    }

    @GetMapping("/api/books/all/{libroId}/copies")
    public List<CopiaDTO> list(@PathVariable Long libroId) {
        return copiaService.findByLibro(libroId);
    }

    @PostMapping("/api/books/add/{libroId}/copies")
    public ResponseEntity<CopiaDTO> add(@PathVariable Long libroId, @Valid @RequestBody CopiaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(copiaService.addCopia(libroId, req));
    }

    @PatchMapping("/api/copies/update/{id}")
    public CopiaDTO updateStato(@PathVariable Long id, @Valid @RequestBody CopiaStatoRequest req) {
        return copiaService.updateStato(id, req.stato());
    }

    @DeleteMapping("/api/copies/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        copiaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
