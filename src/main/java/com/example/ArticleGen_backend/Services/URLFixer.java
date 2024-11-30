package com.example.ArticleGen_backend.Services;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class URLFixer {

    /**
     * Cria uma URL válida codificando espaços e outros caracteres especiais.
     *
     * @param url URL em formato de string.
     * @return Objeto URL válido.
     * @throws Exception Se a URL original não for válida.
     */
    public static URL createValidURL(String url) throws Exception {

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        // Localiza a parte da URL após o domínio
        int indexOfDomainEnd = url.indexOf("/", 8);
        if (indexOfDomainEnd == -1) {
            return new URL(url);
        }

        // Divide em base e caminho/query
        String baseUrl = url.substring(0, indexOfDomainEnd);
        String pathAndQuery = url.substring(indexOfDomainEnd);


        String encodedPathAndQuery = encodePathAndQuery(pathAndQuery);


        return new URL(baseUrl + encodedPathAndQuery);
    }

    /**
     * Codifica o caminho e query da URL, garantindo compatibilidade com padrões web.
     *
     * @param pathAndQuery Caminho e query da URL.
     * @return String codificada.
     */
    private static String encodePathAndQuery(String pathAndQuery) {
        return URLEncoder.encode(pathAndQuery, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20")
                .replaceAll("%2F", "/")
                .replaceAll("%3A", ":")
                .replaceAll("%3F", "?")
                .replaceAll("%26", "&")
                .replaceAll("%3D", "=");
    }

    public static void main(String[] args) {
        try {

            String urlWithSpaces = "https://www.example.com/algoritmos e computação";
            String urlWithoutScheme = "www.example.com/algoritmos e computação";

            System.out.println("URL corrigida: " + createValidURL(urlWithSpaces));
            System.out.println("URL corrigida (sem esquema): " + createValidURL(urlWithoutScheme));
        } catch (Exception e) {
            System.err.println("Erro ao corrigir URL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}