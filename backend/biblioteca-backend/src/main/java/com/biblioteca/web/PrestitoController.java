package com.biblioteca.web;

import com.biblioteca.dto.PrestitoDTO;
import com.biblioteca.dto.PrestitoRequest;
import com.biblioteca.model.enums.StatoPrestito;
import com.biblioteca.service.PrestitoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class PrestitoController {

    private final PrestitoService prestitoService;

    public PrestitoController(PrestitoService prestitoService) {
        this.prestitoService = prestitoService;
    }

    @PostMapping("/create")
    public ResponseEntity<PrestitoDTO> registra(@Valid @RequestBody PrestitoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestitoService.registra(req));
    }

    @PostMapping("/return/{id}")
    public PrestitoDTO restituzione(@PathVariable Long id) {
        return prestitoService.registraRestituzione(id);
    }

    @GetMapping("/all")
    public List<PrestitoDTO> all(@RequestParam(required = false) Long utenteId,
                                 @RequestParam(required = false) StatoPrestito stato) {
        return prestitoService.lista(utenteId, stato);
    }

    @GetMapping("/mine")
    public List<PrestitoDTO> mine(Authentication authentication) {
        return prestitoService.listaPerEmail(authentication.getName());
    }
}
