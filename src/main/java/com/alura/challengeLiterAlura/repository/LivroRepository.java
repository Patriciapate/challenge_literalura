package com.alura.challengeLiterAlura.repository;

import com.alura.challengeLiterAlura.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    Livro findByTitulo(String titulo);

    List<Livro> findByIdioma(String idioma);

}