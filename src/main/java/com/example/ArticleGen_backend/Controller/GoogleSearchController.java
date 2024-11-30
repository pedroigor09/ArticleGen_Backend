package com.example.ArticleGen_backend.Controller;

import com.example.ArticleGen_backend.Services.ContentExtractor;
import com.example.ArticleGen_backend.Services.GoogleSearchService;
import com.example.ArticleGen_backend.Services.PdfGenerator;
import com.itextpdf.text.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GoogleSearchController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSearchController.class);

    @Autowired
    private GoogleSearchService googleSearchService;

    @Autowired
    private ContentExtractor contentExtractor;

    private boolean isValidUrl(String url) {
        try {
            URL parsedUrl = new URL(url);
            return "http".equals(parsedUrl.getProtocol()) || "https".equals(parsedUrl.getProtocol());
        } catch (MalformedURLException e) {
            logger.error("URL malformada: {}", url, e);
            return false;
        }
    }

    @GetMapping("/search")
    public List<String> search(@RequestParam("subject") String subject, @RequestParam("theme") String theme) {
        if (subject == null || subject.isBlank() || theme == null || theme.isBlank()) {
            logger.error("Parâmetros de busca 'subject' e 'theme' não podem ser vazios.");
            throw new IllegalArgumentException("Parâmetros de busca 'subject' e 'theme' não podem ser vazios.");
        }

        try {
            List<String> articles = googleSearchService.searchArticles(subject, theme);
            List<String> extractedContent = articles.stream()
                    .map(articleLink -> {
                        logger.info("Verificando URL: {}", articleLink);
                        String validUrl = contentExtractor.extractValidUrl(articleLink);
                        if (validUrl != null && isValidUrl(validUrl)) {
                            try {
                                return contentExtractor.extractContentFromUrl(validUrl);
                            } catch (IOException e) {
                                logger.warn("Erro ao extrair conteúdo da URL: {}. Ignorando.", validUrl, e);
                            }
                        } else {
                            logger.warn("URL inválida ignorada: {}", articleLink);
                        }
                        return new ArrayList<String>();
                    })
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());

            return extractedContent.isEmpty() ? List.of("Nenhum conteúdo relevante encontrado.") : extractedContent;
        } catch (IOException e) {
            logger.error("Erro ao realizar a busca para o assunto: {} e tema: {}", subject, theme, e);
            throw new RuntimeException("Erro ao realizar a busca", e);
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadPDF(@RequestParam String subject, @RequestParam String theme) throws DocumentException, IOException {
        if (subject == null || subject.isBlank() || theme == null || theme.isBlank()) {
            logger.error("Parâmetros 'subject' ou 'theme' não podem ser vazios.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            List<String> articles = googleSearchService.searchArticles(subject, theme);
            List<String> articleContents = articles.stream()
                    .map(articleLink -> {
                        logger.info("Verificando URL: {}", articleLink);
                        String validUrl = contentExtractor.extractValidUrl(articleLink);
                        if (validUrl != null && isValidUrl(validUrl)) {
                            try {
                                return contentExtractor.extractContentFromUrl(validUrl);
                            } catch (IOException e) {
                                logger.warn("Erro ao extrair conteúdo da URL: {}. Ignorando.", validUrl, e);
                            }
                        } else {
                            logger.warn("URL inválida ignorada: {}", articleLink);
                        }
                        return new ArrayList<String>();
                    })
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());

            if (articleContents.isEmpty()) {
                articleContents.add("Nenhum conteúdo relevante foi encontrado com os parâmetros fornecidos.");
            }

            logger.info("Conteúdo extraído: {}", articleContents);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfGenerator.generatePdf(subject, theme, articleContents, outputStream);
            byte[] pdfBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "artigo.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            logger.error("Erro ao gerar o PDF para o assunto: {} e tema: {}", subject, theme, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
