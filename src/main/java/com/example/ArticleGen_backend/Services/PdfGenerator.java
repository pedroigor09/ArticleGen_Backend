package com.example.ArticleGen_backend.Services;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Service
public class PdfGenerator {

    /**
     * Gera um PDF com o conteúdo extraído das URLs fornecidas.
     *
     * @param assunto       Assunto do documento.
     * @param tema          Tema do documento.
     * @param urls          Lista de URLs para extração de conteúdo.
     * @param outputStream  OutputStream para salvar o PDF gerado.
     */

    private static final Logger logger = LoggerFactory.getLogger(PdfGenerator.class);

    public static void generatePdf(String assunto, String tema, List<String> urls, ByteArrayOutputStream outputStream) {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(new Paragraph("Assunto: " + assunto));
            document.add(new Paragraph("Tema: " + tema));
            document.add(new Paragraph("\n"));

            if (urls != null && !urls.isEmpty()) {
                document.add(new Paragraph("Conteúdo Extraído:"));
                document.add(new Paragraph("\n"));

                for (int i = 0; i < urls.size(); i++) {
                    try {
                        String url = urls.get(i);
                        if (isValidUrl(url)) {
                            String pageContent = extractContentFromUrl(url);
                            addFormattedSection(document, (i + 1) + ". " + url, pageContent);
                        } else {
                            addFormattedSection(document, (i + 1) + ". Texto extraido com Sucesso:", url);
                        }
                    } catch (Exception e) {
                        logger.error("Erro ao processar a URL " + urls.get(i), e);
                        addFormattedSection(document, (i + 1) + ". Erro ao processar URL", "Detalhes: " + e.getMessage());
                    }
                }
            } else {
                document.add(new Paragraph("Nenhuma URL fornecida."));
            }

            document.close();
        } catch (Exception e) {
            logger.error("Erro ao gerar o PDF", e);
            throw new RuntimeException("Erro ao gerar o PDF", e);
        }
    }

    /**
     * Valida se a URL fornecida é válida.
     *
     * @param url URL a ser validada.
     * @return true se for válida, false caso contrário.
     */
    private static boolean isValidUrl(String url) {
        try {
            URL parsedUrl = new URL(url);
            return "http".equals(parsedUrl.getProtocol()) || "https".equals(parsedUrl.getProtocol());
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Extrai o conteúdo textual do corpo de uma URL usando Jsoup.
     *
     * @param url URL da página.
     * @return Conteúdo textual da página.
     */
    private static String extractContentFromUrl(String url) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            return doc.body().text();
        } catch (Exception e) {
            logger.warn("Erro ao acessar a URL: " + url, e);
            return "Erro ao acessar a URL.";
        }
    }

    /**
     * Adiciona uma seção formatada ao PDF.
     *
     * @param document Documento PDF.
     * @param title    Título da seção.
     * @param content  Conteúdo da seção.
     * @throws Exception Em caso de erro ao adicionar o conteúdo.
     */
    private static void addFormattedSection(Document document, String title, String content) throws Exception {
        Paragraph titleParagraph = new Paragraph(title);
        titleParagraph.setAlignment(Element.ALIGN_LEFT);
        titleParagraph.setSpacingAfter(5);

        Paragraph contentParagraph = new Paragraph(content);
        contentParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
        contentParagraph.setSpacingAfter(15);

        document.add(titleParagraph);
        document.add(contentParagraph);
    }
}