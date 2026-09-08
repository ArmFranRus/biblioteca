package com.biblioteca.model;

import com.biblioteca.model.enums.StatoPrestito;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "prestito")
@Getter
@Setter
@NoArgsConstructor
public class Prestito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "copia_id", nullable = false)
    private Copia copia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(name = "data_prestito", nullable = false)
    private LocalDate dataPrestito;

    @Column(name = "data_scadenza", nullable = false)
    private LocalDate dataScadenza;

    @Column(name = "data_restituzione")
    private LocalDate dataRestituzione;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatoPrestito stato = StatoPrestito.ATTIVO;
}
