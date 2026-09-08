package com.biblioteca.service;

import com.biblioteca.dto.AutoreDTO;
import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.dto.LibroDTO;
import com.biblioteca.dto.LibroRequest;
import com.biblioteca.model.Autore;
import com.biblioteca.model.Categoria;
import com.biblioteca.model.Libro;
import com.biblioteca.model.enums.StatoCopia;
import com.biblioteca.repository.AutoreRepository;
import com.biblioteca.repository.CategoriaRepository;
import com.biblioteca.repository.CopiaRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.RecensioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final AutoreRepository autoreRepository;
    private final CopiaRepository copiaRepository;
    private final RecensioneRepository recensioneRepository;

    public LibroService(LibroRepository libroRepository,
                        CategoriaRepository categoriaRepository,
                        AutoreRepository autoreRepository,
                        CopiaRepository copiaRepository,
                        RecensioneRepository recensioneRepository) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
        this.autoreRepository = autoreRepository;
        this.copiaRepository = copiaRepository;
        this.recensioneRepository = recensioneRepository;
    }

    @Transactional(readOnly = true)
    public List<LibroDTO> search(String titolo, Long categoriaId, String autore) {
        String t = (titolo == null || titolo.isBlank()) ? null : titolo;
        String a = (autore == null || autore.isBlank()) ? null : autore;
        return libroRepository.search(t, categoriaId, a).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public LibroDTO findById(Long id) {
        return toDTO(getEntity(id));
    }

    @Transactional
    public LibroDTO create(LibroRequest req) {
        if (req.isbn() != null && !req.isbn().isBlank() && libroRepository.existsByIsbn(req.isbn())) {
            throw new ConflittoException("ISBN gia' presente: " + req.isbn());
        }
        Libro libro = new Libro();
        applyRequest(libro, req);
        return toDTO(libroRepository.save(libro));
    }

    @Transactional
    public LibroDTO update(Long id, LibroRequest req) {
        Libro libro = getEntity(id);
        if (req.isbn() != null && !req.isbn().isBlank()
                && !req.isbn().equals(libro.getIsbn())
                && libroRepository.existsByIsbn(req.isbn())) {
            throw new ConflittoException("ISBN gia' presente: " + req.isbn());
        }
        applyRequest(libro, req);
        return toDTO(libroRepository.save(libro));
    }

    @Transactional
    public void delete(Long id) {
        Libro libro = getEntity(id);
        long inPrestito = copiaRepository.countByLibroIdAndStato(id, StatoCopia.PRESTATA);
        if (inPrestito > 0) {
            throw new ConflittoException(
                    "Impossibile eliminare il libro: " + inPrestito + " copia/e attualmente in prestito.");
        }
        recensioneRepository.deleteAll(recensioneRepository.findByLibroId(id));
        copiaRepository.deleteAll(copiaRepository.findByLibroId(id));
        libroRepository.delete(libro);
    }

    private void applyRequest(Libro libro, LibroRequest req) {
        libro.setTitolo(req.titolo());
        libro.setIsbn((req.isbn() == null || req.isbn().isBlank()) ? null : req.isbn());
        libro.setEditore(req.editore());
        libro.setAnno(req.anno());

        if (req.categoriaId() != null) {
            Categoria c = categoriaRepository.findById(req.categoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", req.categoriaId()));
            libro.setCategoria(c);
        } else {
            libro.setCategoria(null);
        }

        libro.getAutori().clear();
        if (req.autoriIds() != null) {
            for (Long autoreId : req.autoriIds()) {
                Autore a = autoreRepository.findById(autoreId)
                        .orElseThrow(() -> new ResourceNotFoundException("Autore", autoreId));
                libro.getAutori().add(a);
            }
        }
    }

    private Libro getEntity(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", id));
    }

    private LibroDTO toDTO(Libro l) {
        CategoriaDTO categoriaDTO = (l.getCategoria() == null) ? null
                : new CategoriaDTO(l.getCategoria().getId(), l.getCategoria().getNome());

        List<AutoreDTO> autoriDTO = l.getAutori().stream()
                .map(a -> new AutoreDTO(a.getId(), a.getNome()))
                .sorted(Comparator.comparing(AutoreDTO::nome))
                .toList();

        long totali = copiaRepository.countByLibroId(l.getId());
        long disponibili = copiaRepository.countByLibroIdAndStato(l.getId(), StatoCopia.DISPONIBILE);
        Double votoMedio = recensioneRepository.avgVoto(l.getId());
        long numeroRecensioni = recensioneRepository.countByLibroId(l.getId());

        return new LibroDTO(l.getId(), l.getTitolo(), l.getIsbn(), l.getEditore(), l.getAnno(),
                categoriaDTO, autoriDTO, totali, disponibili, votoMedio, numeroRecensioni);
    }
}
