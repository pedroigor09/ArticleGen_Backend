package com.example.ArticleGen_backend.Services;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleSearchService {

    private static final String API_KEY = "AIzaSyCGdgfBpmM0drpyvSjXhTSXbZmCzDbipe0";
    private static final String CX_ID = "908cc48b8bc524f5d";
    private static final String GOOGLE_SEARCH_API_URL = "https://www.googleapis.com/customsearch/v1";

    public List<String> searchArticles(String subject, String theme) throws IOException {
        String query = URLEncoder.encode(subject + " " + theme, StandardCharsets.UTF_8);


        String url = String.format("%s?key=%s&cx=%s&q=%s", GOOGLE_SEARCH_API_URL, API_KEY, CX_ID, query);


        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode != 200) {
                    throw new IOException("Erro na API Google. Status Code: " + statusCode);
                }
                String jsonResponse = EntityUtils.toString(response.getEntity());
                return parseSearchResults(jsonResponse);
            } catch (ParseException e) {
                throw new IOException("Erro ao processar a resposta da API.", e);
            }
        }
    }

    /**
     * Processa os resultados da resposta JSON.
     *
     * @param jsonResponse Resposta da API em formato JSON.
     * @return Lista de resultados formatados.
     */
    private List<String> parseSearchResults(String jsonResponse) {
        List<String> results = new ArrayList<>();
        JSONObject json = new JSONObject(jsonResponse);


        if (json.optJSONArray("items") != null) {
            JSONArray items = json.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String title = item.optString("title", "Sem título");
                String link = item.optString("link", "Sem link");

                // Adiciona apenas URLs válidas
                if (isValidUrl(link)) {
                    results.add(title + " - " + link);
                }
            }
        } else {
            results.add("Nenhum resultado encontrado.");
        }

        return results;
    }

    /**
     * Valida uma URL usando URI.
     *
     * @param url URL a ser validada.
     * @return true se a URL for válida, false caso contrário.
     */
    private boolean isValidUrl(String url) {
        try {
            new URI(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }
}
