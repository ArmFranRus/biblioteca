package com.biblioteca.service;

import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.dto.CategoriaRequest;
import com.biblioteca.model.Categoria;
import com.biblioteca.repository.CategoriaRepository;
import com.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, LibroRepository libroRepository) {
        this.categoriaRepository = categoriaRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> findAll() {
        return categoriaRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO findById(Long id) {
        return toDTO(getEntity(id));
    }

    @Transactional
    public CategoriaDTO create(CategoriaRequest req) {
        if (categoriaRepository.existsByNome(req.nome())) {
            throw new ConflittoException("Categoria gia' esistente: " + req.nome());
        }
        Categoria c = new Categoria();
        c.setNome(req.nome());
        return toDTO(categoriaRepository.save(c));
    }

    @Transactional
    public CategoriaDTO update(Long id, CategoriaRequest req) {
        Categoria c = getEntity(id);
        if (!c.getNome().equals(req.nome()) && categoriaRepository.existsByNome(req.nome())) {
            throw new ConflittoException("Categoria gia' esistente: " + req.nome());
        }
        c.setNome(req.nome());
        return toDTO(categoriaRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        Categoria c = getEntity(id);
        if (libroRepository.countByCategoriaId(id) > 0) {
            throw new ConflittoException("Categoria in uso da uno o piu' libri: impossibile eliminarla.");
        }
        categoriaRepository.delete(c);
    }

    private Categoria getEntity(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    private CategoriaDTO toDTO(Categoria c) {
        return new CategoriaDTO(c.getId(), c.getNome());
    }
}
