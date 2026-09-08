package com.biblioteca.repository;

import com.biblioteca.model.Prestito;
import com.biblioteca.model.enums.StatoPrestito;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PrestitoRepository extends JpaRepository<Prestito, Long> {

    List<Prestito> findByUtenteIdOrderByDataPrestitoDesc(Long utenteId);

    List<Prestito> findByStatoOrderByDataPrestitoDesc(StatoPrestito stato);

    List<Prestito> findAllByOrderByDataPrestitoDesc();

    long countByUtenteIdAndStatoIn(Long utenteId, Collection<StatoPrestito> stati);

    List<Prestito> findByStatoAndDataScadenzaBefore(StatoPrestito stato, LocalDate data);

    long countByStatoIn(Collection<StatoPrestito> stati);

    long countByDataRestituzioneIsNullAndDataScadenzaBefore(LocalDate data);

    @Query("""
            select l.id, l.titolo, count(p)
            from Prestito p
            join p.copia cop
            join cop.libro l
            group by l.id, l.titolo
            order by count(p) desc
            """)
    List<Object[]> titoliPiuPrestati(Pageable pageable);
}
