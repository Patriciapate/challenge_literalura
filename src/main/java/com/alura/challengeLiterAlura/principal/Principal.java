package com.alura.challengeLiterAlura.principal;

import com.alura.challengeLiterAlura.model.Autor;
import com.alura.challengeLiterAlura.model.Livro;
import com.alura.challengeLiterAlura.repository.AutorRepository;
import com.alura.challengeLiterAlura.repository.LivroRepository;
import com.alura.challengeLiterAlura.service.ConsumoApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Scanner;

@Component
public class Principal implements CommandLineRunner {

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private ConsumoApi consumoAPI;

    private Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) throws JSONException {

        int opcao;

        do {
            System.out.println("""
                    ------------------------------
                    Escolha o número de sua opção:
                    1- buscar livro pelo título
                    2- listar livros registrados
                    3- listar autores registrados
                    4- listar autores vivos em um determinado ano
                    5- listar livros em um determinado idioma
                    0 - sair
                    """);

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    buscarLivro();
                    break;
                case 2:
                    listarLivros();
                    break;
                case 3:
                    listarAutores();
                    break;
                case 4:
                    listarAutoresVivos();
                    break;
                case 5:
                    listarPorIdioma();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida!");
            }

        } while (opcao != 0);
    }

    private void buscarLivro() throws JSONException {

        System.out.println("Insira o nome do livro que você deseja procurar: ");
        String tituloBusca = scanner.nextLine();

        String json = consumoAPI.buscarLivro(tituloBusca);

        JSONObject obj = new JSONObject(json);
        JSONArray resultados = obj.getJSONArray("results");

        if (resultados.length() == 0) {
            System.out.println("Livro não encontrado!");
            return;
        }

        JSONObject livroJson = resultados.getJSONObject(0);

        String titulo = livroJson.getString("title");
        Double downloads = livroJson.getDouble("download_count");
        String idioma = livroJson.getJSONArray("languages").getString(0);

        JSONObject autorJson = livroJson.getJSONArray("authors").getJSONObject(0);

        String nomeAutor = autorJson.getString("name");
        Integer nascimento = autorJson.optInt("birth_year");
        Integer falecimento = autorJson.optInt("death_year");

        // Verifica se autor já existe
        Autor autor = autorRepository.findByNome(nomeAutor);
        if (autor == null) {
            autor = new Autor(nomeAutor, nascimento, falecimento);
            autorRepository.save(autor);
        }

        // Verifica se livro já existe
        Livro livroExistente = livroRepository.findByTitulo(titulo);

        if (livroExistente != null) {
            System.out.println("\nLivro já existe no banco:");
            System.out.println("Título: " + livroExistente.getTitulo());
            System.out.println("Autor: " + livroExistente.getAutor().getNome());
            System.out.println();
            return;
        }

        Livro livro = new Livro(titulo, idioma, downloads, autor);
        livroRepository.save(livro);

        // MOSTRAR NA TELA
        System.out.println("\n----- LIVRO SALVO -----");
        System.out.println("Título: " + titulo);
        System.out.println("Autor: " + nomeAutor);
        System.out.println("Idioma: " + idioma);
        System.out.println("Downloads: " + downloads);
        System.out.println("-----------------------\n");
    }

    private void listarLivros() {
        List<Livro> livros = livroRepository.findAll();

        if (livros.isEmpty()) {
            System.out.println("Nenhum livro registrado no banco de dados.");
            return;
        }

        livros.forEach(l -> {
            System.out.println("----- LIVRO -----");
            System.out.println("Título: " + l.getTitulo());
            System.out.println("Autor: " + l.getAutor().getNome());
            System.out.println("Idioma: " + l.getIdioma());
            System.out.println("Número de downloads: " + l.getNumeroDownloads());
            System.out.println("----------------");
        });
    }

    @Transactional
    private void listarAutores() {
        List<Autor> autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("Nenhum autor registrado no banco de dados.");
            return;
        }

        autores.forEach(a -> {
            System.out.println("Autor: " + a.getNome());
            System.out.println("Ano de nascimento: " + a.getAnoNascimento());
            System.out.println("Ano de falecimento: " + a.getAnoFalecimento());

            System.out.println("Livros:");
            a.getLivros().forEach(l ->
                    System.out.println(" - " + l.getTitulo())
            );

            System.out.println();
        });
    }

    @Transactional
    private void listarAutoresVivos() {

        System.out.println("Insira o ano que deseja pesquisar:");
        int ano = scanner.nextInt();
        scanner.nextLine();

        List<Autor> autores = autorRepository.autoresVivosNoAno(ano);

        if (autores.isEmpty()) {
            System.out.println("Nenhum autor estava vivo no ano informado.");
            return;
        }

        autores.forEach(a -> {
            System.out.println("Autor: " + a.getNome());
            System.out.println("Ano de nascimento: " + a.getAnoNascimento());
            System.out.println("Ano de falecimento: " + a.getAnoFalecimento());

            System.out.println("Livros:");
            a.getLivros().forEach(l ->
                    System.out.println(" - " + l.getTitulo())
            );

            System.out.println();
        });
    }

    private void listarPorIdioma() {

        System.out.println("""
                Insira o idioma para realizar a busca:
                es - espanhol
                en - inglês
                fr - francês
                pt - português
                """);

        String idioma = scanner.nextLine();

        List<Livro> livros = livroRepository.findByIdioma(idioma);

        if (livros.isEmpty()) {
            System.out.println("Nenhum livro encontrado para o idioma informado.");
            return;
        }

        livros.forEach(l -> {
            System.out.println("----- LIVRO -----");
            System.out.println("Título: " + l.getTitulo());
            System.out.println("Autor: " + l.getAutor().getNome());
            System.out.println("Idioma: " + l.getIdioma());
            System.out.println("Número de downloads: " + l.getNumeroDownloads());
            System.out.println("----------------");
        });
    }
}