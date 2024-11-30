package com.example.ArticleGen_backend.Services;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class SavePdf {
    public static void savePdf(ByteArrayOutputStream outputStream, String fileName) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            outputStream.writeTo(fileOutputStream);
        }
    }
}
