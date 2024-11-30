package com.example.ArticleGen_backend.Services;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TranslationService {

    private static final int MAX_TEXT_LENGTH = 204800; // Limite de bytes por requisição

    private final Translate translate;

    public TranslationService(@Value("${google.cloud.api.key}") String apiKey) {
        this.translate = TranslateOptions.newBuilder()
                .setApiKey(apiKey)
                .build()
                .getService();
    }

    public String translateToPortuguese(String text) {
        if (text.getBytes().length > MAX_TEXT_LENGTH) {
            List<String> parts = splitTextIntoParts(text, MAX_TEXT_LENGTH);
            StringBuilder translatedText = new StringBuilder();

            for (String part : parts) {
                Translation translation = translate.translate(part, Translate.TranslateOption.targetLanguage("pt"));
                translatedText.append(translation.getTranslatedText());
            }
            return translatedText.toString();
        } else {
            Translation translation = translate.translate(text, Translate.TranslateOption.targetLanguage("pt"));
            return translation.getTranslatedText();
        }
    }

    private List<String> splitTextIntoParts(String text, int maxBytes) {
        List<String> parts = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < text.length()) {
            int endIndex = Math.min(text.length(), startIndex + maxBytes);
            String part = text.substring(startIndex, endIndex);
            while (part.getBytes().length > maxBytes) {
                endIndex--;
                part = text.substring(startIndex, endIndex);
            }
            parts.add(part);
            startIndex = endIndex;
        }

        return parts;
    }
}
