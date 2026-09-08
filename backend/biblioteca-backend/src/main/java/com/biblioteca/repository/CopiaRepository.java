package com.biblioteca.repository;

import com.biblioteca.model.Copia;
import com.biblioteca.model.enums.StatoCopia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CopiaRepository extends JpaRepository<Copia, Long> {
    List<Copia> findByLibroId(Long libroId);
    List<Copia> findByLibroIdAndStato(Long libroId, StatoCopia stato);
    long countByLibroId(Long libroId);
    long countByLibroIdAndStato(Long libroId, StatoCopia stato);
    long countByStato(StatoCopia stato);
    boolean existsByCodiceInventario(String codiceInventario);
}
