package com.biblioteca.repository;

import com.biblioteca.model.Autore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutoreRepository extends JpaRepository<Autore, Long> {
    List<Autore> findByNomeContainingIgnoreCase(String nome);
}
