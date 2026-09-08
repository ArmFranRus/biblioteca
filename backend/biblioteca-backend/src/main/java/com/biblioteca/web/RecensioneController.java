package com.biblioteca.web;

import com.biblioteca.dto.RecensioneDTO;
import com.biblioteca.dto.RecensioneRequest;
import com.biblioteca.service.RecensioneService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class RecensioneController {

    private final RecensioneService recensioneService;

    public RecensioneController(RecensioneService recensioneService) {
        this.recensioneService = recensioneService;
    }

    @PostMapping("/create")
    public ResponseEntity<RecensioneDTO> crea(@Valid @RequestBody RecensioneRequest req,
                                              Authentication authentication) {
        RecensioneDTO dto = recensioneService.crea(req, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/all/{libroId}")
    public List<RecensioneDTO> perLibro(@PathVariable Long libroId, Authentication authentication) {
        String email = (authentication != null) ? authentication.getName() : null;
        return recensioneService.listaPerLibro(libroId, email);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id, Authentication authentication) {
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
        recensioneService.elimina(id, authentication.getName(), staff);
        return ResponseEntity.noContent().build();
    }
}
