package com.biblioteca.web;

import com.biblioteca.dto.StaffCreateRequest;
import com.biblioteca.dto.UtenteDTO;
import com.biblioteca.service.UtenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping("/all")
    public List<UtenteDTO> all() {
        return utenteService.findAll();
    }

    @GetMapping("/search")
    public List<UtenteDTO> search(@RequestParam(required = false) String q) {
        return utenteService.search(q);
    }

    @GetMapping("/find/{id}")
    public UtenteDTO find(@PathVariable Long id) {
        return utenteService.findById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<UtenteDTO> createStaff(@Valid @RequestBody StaffCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utenteService.createStaff(req));
    }

    @PatchMapping("/enable/{id}")
    public UtenteDTO enable(@PathVariable Long id) {
        return utenteService.setAttivo(id, true);
    }

    @PatchMapping("/disable/{id}")
    public UtenteDTO disable(@PathVariable Long id) {
        return utenteService.setAttivo(id, false);
    }
}
