package com.example.ArticleGen_backend;

import com.example.ArticleGen_backend.Services.PdfGenerator;
import com.example.ArticleGen_backend.Services.SavePdf;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ArticleGenBackendApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(ArticleGenBackendApplication.class, args);

		// Informações para o PDF
		String assunto = "Alan Turing";
		String tema = "Computação";
		List<String> atividades = Arrays.asList(
				"Explique o que é a Máquina de Turing.",
				"Como Alan Turing contribuiu para a computação moderna?"
		);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // Usando o ByteArrayOutputStream correto

		// Passando todos os parâmetros necessários para o método generatePdf
		PdfGenerator.generatePdf(assunto, tema, atividades, outputStream);

		// Agora, salve o PDF usando o método SavePdf
		SavePdf.savePdf(outputStream, "Atividades_Alan_Turing_Computacao.pdf");
	}

	@Value("${PORT:8080}")
	private String port;

	@Bean
	public TomcatServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
		factory.setPort(Integer.parseInt(port));
		return factory;
	}
}
