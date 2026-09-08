package com.biblioteca.web;

import com.biblioteca.dto.PrenotazioneDTO;
import com.biblioteca.dto.PrenotazioneRequest;
import com.biblioteca.model.enums.StatoPrenotazione;
import com.biblioteca.service.PrenotazioneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class PrenotazioneController {

    private final PrenotazioneService prenotazioneService;

    public PrenotazioneController(PrenotazioneService prenotazioneService) {
        this.prenotazioneService = prenotazioneService;
    }

    @PostMapping("/create")
    public ResponseEntity<PrenotazioneDTO> prenota(@Valid @RequestBody PrenotazioneRequest req,
                                                   Authentication authentication) {
        PrenotazioneDTO dto = prenotazioneService.prenota(req.libroId(), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<Void> annulla(@PathVariable Long id, Authentication authentication) {
        prenotazioneService.annulla(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public List<PrenotazioneDTO> mine(Authentication authentication) {
        return prenotazioneService.listaPerEmail(authentication.getName());
    }

    @GetMapping("/all")
    public List<PrenotazioneDTO> all(@RequestParam(required = false) StatoPrenotazione stato) {
        return prenotazioneService.lista(stato);
    }
}
