package com.biblioteca.service;

import com.biblioteca.dto.CopiaDTO;
import com.biblioteca.dto.CopiaRequest;
import com.biblioteca.model.Copia;
import com.biblioteca.model.Libro;
import com.biblioteca.model.enums.StatoCopia;
import com.biblioteca.repository.CopiaRepository;
import com.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CopiaService {

    private final CopiaRepository copiaRepository;
    private final LibroRepository libroRepository;

    public CopiaService(CopiaRepository copiaRepository, LibroRepository libroRepository) {
        this.copiaRepository = copiaRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<CopiaDTO> findByLibro(Long libroId) {
        if (!libroRepository.existsById(libroId)) {
            throw new ResourceNotFoundException("Libro", libroId);
        }
        return copiaRepository.findByLibroId(libroId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public CopiaDTO addCopia(Long libroId, CopiaRequest req) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", libroId));
        if (copiaRepository.existsByCodiceInventario(req.codiceInventario())) {
            throw new ConflittoException("Codice inventario gia' esistente: " + req.codiceInventario());
        }
        Copia copia = new Copia();
        copia.setLibro(libro);
        copia.setCodiceInventario(req.codiceInventario());
        copia.setStato(StatoCopia.DISPONIBILE);
        return toDTO(copiaRepository.save(copia));
    }

    @Transactional
    public CopiaDTO updateStato(Long copiaId, StatoCopia nuovoStato) {
        Copia copia = copiaRepository.findById(copiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Copia", copiaId));

        // Lo stato PRESTATA e' determinato esclusivamente dai prestiti.
        if (nuovoStato == StatoCopia.PRESTATA) {
            throw new ConflittoException(
                    "Lo stato 'PRESTATA' e' gestito dai prestiti e non puo' essere impostato manualmente.");
        }
        if (copia.getStato() == StatoCopia.PRESTATA) {
            throw new ConflittoException(
                    "La copia e' attualmente in prestito: registra prima la restituzione.");
        }

        copia.setStato(nuovoStato);
        return toDTO(copiaRepository.save(copia));
    }

    @Transactional
    public void delete(Long copiaId) {
        Copia copia = copiaRepository.findById(copiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Copia", copiaId));
        if (copia.getStato() == StatoCopia.PRESTATA) {
            throw new ConflittoException("Impossibile eliminare una copia attualmente in prestito.");
        }
        copiaRepository.delete(copia);
    }

    private CopiaDTO toDTO(Copia c) {
        return new CopiaDTO(c.getId(), c.getLibro().getId(), c.getCodiceInventario(), c.getStato());
    }
}
