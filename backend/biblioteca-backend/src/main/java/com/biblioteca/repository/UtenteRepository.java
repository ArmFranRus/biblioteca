package com.biblioteca.repository;

import com.biblioteca.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Optional<Utente> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            select u from Utente u
            where lower(u.email) like lower(concat('%', :q, '%'))
               or lower(u.nome) like lower(concat('%', :q, '%'))
               or lower(u.cognome) like lower(concat('%', :q, '%'))
            order by u.cognome, u.nome
            """)
    List<Utente> search(@Param("q") String q);
}
