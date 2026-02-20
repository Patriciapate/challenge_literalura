package com.alura.challengeLiterAlura.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConsumoApi {

    private final String URL = "https://gutendex.com/books/?search=";

    public String buscarLivro(String titulo) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(URL + titulo.replace(" ", "%20"), String.class);
    }
}