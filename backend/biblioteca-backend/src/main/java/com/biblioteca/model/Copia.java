package com.biblioteca.model;

import com.biblioteca.model.enums.StatoCopia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "copia")
@Getter
@Setter
@NoArgsConstructor
public class Copia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(name = "codice_inventario", nullable = false, unique = true, length = 40)
    private String codiceInventario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatoCopia stato = StatoCopia.DISPONIBILE;
}
