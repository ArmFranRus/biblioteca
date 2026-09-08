package com.biblioteca.service;

import com.biblioteca.dto.AutoreDTO;
import com.biblioteca.dto.AutoreRequest;
import com.biblioteca.model.Autore;
import com.biblioteca.repository.AutoreRepository;
import com.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AutoreService {

    private final AutoreRepository autoreRepository;
    private final LibroRepository libroRepository;

    public AutoreService(AutoreRepository autoreRepository, LibroRepository libroRepository) {
        this.autoreRepository = autoreRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<AutoreDTO> findAll() {
        return autoreRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public AutoreDTO findById(Long id) {
        return toDTO(getEntity(id));
    }

    @Transactional
    public AutoreDTO create(AutoreRequest req) {
        Autore a = new Autore();
        a.setNome(req.nome());
        return toDTO(autoreRepository.save(a));
    }

    @Transactional
    public AutoreDTO update(Long id, AutoreRequest req) {
        Autore a = getEntity(id);
        a.setNome(req.nome());
        return toDTO(autoreRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        Autore a = getEntity(id);
        if (libroRepository.countByAutori_Id(id) > 0) {
            throw new ConflittoException("Autore associato a uno o piu' libri: impossibile eliminarlo.");
        }
        autoreRepository.delete(a);
    }

    private Autore getEntity(Long id) {
        return autoreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autore", id));
    }

    private AutoreDTO toDTO(Autore a) {
        return new AutoreDTO(a.getId(), a.getNome());
    }
}
