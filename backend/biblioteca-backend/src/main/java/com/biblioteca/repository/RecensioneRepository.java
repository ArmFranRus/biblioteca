package com.biblioteca.repository;

import com.biblioteca.model.Recensione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecensioneRepository extends JpaRepository<Recensione, Long> {

    List<Recensione> findByLibroId(Long libroId);

    List<Recensione> findByLibroIdOrderByDataDesc(Long libroId);

    boolean existsByLibroIdAndUtenteId(Long libroId, Long utenteId);

    long countByLibroId(Long libroId);

    @Query("select avg(r.voto) from Recensione r where r.libro.id = :libroId")
    Double avgVoto(@Param("libroId") Long libroId);
}
