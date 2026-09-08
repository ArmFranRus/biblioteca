package com.biblioteca.repository;

import com.biblioteca.model.Prenotazione;
import com.biblioteca.model.enums.StatoPrenotazione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PrenotazioneRepository extends JpaRepository<Prenotazione, Long> {

    List<Prenotazione> findByUtenteIdOrderByDataDesc(Long utenteId);

    List<Prenotazione> findAllByOrderByDataDesc();

    List<Prenotazione> findByStatoOrderByDataDesc(StatoPrenotazione stato);

    Optional<Prenotazione> findFirstByLibroIdAndStatoOrderByDataAsc(Long libroId, StatoPrenotazione stato);

    Optional<Prenotazione> findFirstByLibroIdAndUtenteIdAndStatoOrderByDataAsc(
            Long libroId, Long utenteId, StatoPrenotazione stato);

    boolean existsByUtenteIdAndLibroIdAndStatoIn(
            Long utenteId, Long libroId, Collection<StatoPrenotazione> stati);

    long countByLibroIdAndStatoAndUtenteIdNot(Long libroId, StatoPrenotazione stato, Long utenteId);

    List<Prenotazione> findByStatoAndScadenzaRitiroBefore(StatoPrenotazione stato, LocalDate data);

    long countByStato(StatoPrenotazione stato);
}
