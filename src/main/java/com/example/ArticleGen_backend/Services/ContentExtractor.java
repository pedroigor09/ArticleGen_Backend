package com.example.ArticleGen_backend.Services;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ContentExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ContentExtractor.class);

    private static final int TIMEOUT = 30000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, como Gecko) Chrome/87.0.4280.66 Safari/537.36";

    @Autowired
    private TranslationService translationService;

    public List<String> extractContentFromUrl(String url) throws IOException {
        String validUrl = extractValidUrl(url);
        if (validUrl == null || validUrl.isEmpty()) {
            logger.warn("URL inválida ou malformada: {}", url);
            throw new IllegalArgumentException("A URL fornecida é inválida ou malformada.");
        }

        try {
            Document doc = Jsoup.connect(validUrl)
                    .timeout(TIMEOUT)
                    .userAgent(USER_AGENT)
                    .get();

            Elements contentElements = doc.select("article, section, div.content, main p, main, body > p, div#main-content");
            contentElements.select("nav, header, footer, aside, .sidebar, .menu, .navigation, script, style, advert, span, iframe").remove();

            if (contentElements.isEmpty()) {
                logger.warn("Nenhum conteúdo relevante encontrado nos seletores padrão. Tente ajustar a extração.");
                return List.of();
            }

            List<String> extractedContent = new ArrayList<>();
            for (Element element : contentElements) {
                String text = removeHtmlTags(element);
                if (isValidContent(text)) {
                    String translatedText = translationService.translateToPortuguese(text);
                    extractedContent.add(translatedText);
                }
            }

            List<String> filteredContent = extractedContent.stream()
                    .filter(content -> !content.isBlank() && content.split("\\s+").length > 50 && !isInvalidContent(content))
                    .distinct()
                    .sorted((c1, c2) -> Integer.compare(c2.split("\\s+").length, c1.split("\\s+").length))
                    .collect(Collectors.toList());

            return formatContent(filteredContent);
        } catch (UnsupportedMimeTypeException e) {
            logger.error("Erro de tipo MIME não suportado na URL: {}", validUrl, e);
            return List.of();
        } catch (IOException e) {
            logger.error("Erro ao conectar à URL: {}", validUrl, e);
            throw new IOException("Erro ao acessar a URL: " + validUrl, e);
        }
    }

    private String removeHtmlTags(Element element) {
        String text = element.html();
        text = StringEscapeUtils.unescapeHtml4(text);
        text = text.replaceAll("\\<.*?\\>", ""); // Remove todas as tags HTML
        return Jsoup.parse(text).text();
    }

    private List<String> formatContent(List<String> extractedContent) {
        return extractedContent.stream()
                .filter(content -> !content.isBlank())
                .map(content -> StringEscapeUtils.unescapeHtml4(content))
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isValidContent(String content) {
        return content != null && !content.isBlank() && content.split("\\s+").length > 50 && !content.equals("Nenhum conteúdo relevante encontrado.");
    }

    private boolean isInvalidContent(String content) {
        String[] invalidKeywords = {
                "Imagem interativa", "script", "style", "JavaScript", "Ver",
                "Nenhum conteúdo relevante encontrado", "Erro de tipo MIME não suportado",
                "Anúncio", "Publicidade", "Serviços Corporativos", "Prezada Prudência", "Manuscritos Filosóficos",
                "Benardete Paradoxos", "Filosofia Africana", "Ralph Lee", "Mehari Worku", "Wendy Laura Belcher"
        };
        for (String keyword : invalidKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        if (content.split("\\s+").length < 50) {
            return true;
        }
        return false;
    }

    public String extractValidUrl(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile("https?://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?");
        Matcher matcher = pattern.matcher(text.trim());
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
