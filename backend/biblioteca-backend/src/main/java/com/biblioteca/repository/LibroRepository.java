package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);

    long countByCategoriaId(Long categoriaId);

    long countByAutori_Id(Long autoreId);

    @Query("""
            select distinct l from Libro l
            left join l.autori a
            where (:titolo is null or lower(l.titolo) like lower(concat('%', :titolo, '%')))
              and (:categoriaId is null or l.categoria.id = :categoriaId)
              and (:autore is null or lower(a.nome) like lower(concat('%', :autore, '%')))
            """)
    List<Libro> search(@Param("titolo") String titolo,
                       @Param("categoriaId") Long categoriaId,
                       @Param("autore") String autore);
}
