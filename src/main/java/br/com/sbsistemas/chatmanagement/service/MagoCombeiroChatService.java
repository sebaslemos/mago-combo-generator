package br.com.sbsistemas.chatmanagement.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@BrowserCallable
@AnonymousAllowed
@Service
public class MagoCombeiroChatService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MagoCombeiroChatService.class);

	@Autowired
	@Qualifier("geminiAiService")
	private AiService geminiChatService;

	@Autowired
	@Qualifier("openAiService")
	private AiService openAiChatService;

	@Autowired
	private EmbeddingStoreIngestor ingestor;

	@Autowired
	private JedisPool jedisPool;

	@Value("${langchain4j.openai.api-key:${OPENAI_API_KEY:}}")
	private String openAiApiKey;

	private static final String MANUAL_PERSONAGENS_INGESTION_FLAG = "mago_combo_document_ingested_manual_personagens";

	// private static final String DOCUMENT_HASH_KEY =
	// "mago_combo:document_hash:portaria";

	public MagoCombeiroChatService() {
	}

	public String conversar(String pergunta) throws IOException {
		String result = geminiChatService.chat(pergunta);
		return result;
	}

	public String conversarOpenAi(String pergunta) throws IOException {
		String result = openAiChatService.chat(pergunta);
		return result;
	}

	public void clearDocument() {
		try (Jedis jedis = jedisPool.getResource()) {
			// Remove a flag de ingestão
			jedis.del(MANUAL_PERSONAGENS_INGESTION_FLAG);

			jedis.eval("for i, name in ipairs(redis.call('KEYS', 'mago_combo*')) do redis.call('DEL', name); end", 0);
		}
	}

	public boolean isDocumentIngested() {
		try (Jedis jedis = jedisPool.getResource()) {
			return jedis.exists(MANUAL_PERSONAGENS_INGESTION_FLAG);
		}
	}

	@PostConstruct
	public void initializeDocumentIngestion() {
		if (openAiApiKey == null || openAiApiKey.isBlank()) {
			LOGGER.warn("OpenAI API key ausente. Ingestão do documento será ignorada na inicialização.");
			return;
		}

		// clearDocument();
		try (Jedis jedis = jedisPool.getResource()) {
			if (!isDocumentIngested()) {
				URL fileUrl = MagoCombeiroChatService.class.getClassLoader().getResource("T20-regras.pdf");
				if (fileUrl == null) {
					throw new RuntimeException("Arquivo não encontrado no classpath");
				}
				Path filePath = Paths.get(fileUrl.toURI());
				Document document = FileSystemDocumentLoader.loadDocument(filePath, new ApachePdfBoxDocumentParser());

				LOGGER.info("Documento carregado. Tamanho: {} caracteres", document.text().length());
				ingestor.ingest(document);
				LOGGER.info("Ingestão concluída.");

				// Marca como processado
				jedis.set(MANUAL_PERSONAGENS_INGESTION_FLAG, "true");
			} else {
				LOGGER.info("Documento já foi ingerido anteriormente.");
			}
		} catch (AuthenticationException e) {
			LOGGER.warn(
					"Falha de autenticação ao ingerir documento (verifique OPENAI_API_RPG/OPENAI_API_KEY). Inicialização continuará sem ingestão.");
		} catch (RateLimitException e) {
			LOGGER.warn(
					"OpenAI sem cota disponível no momento (insufficient_quota). Inicialização continuará sem ingestão.");
		} catch (RuntimeException e) {
			LOGGER.warn("Falha ao ingerir documento na inicialização. A aplicação continuará sem ingestão.", e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Erro ao carregar o documento", e);
		}
	}
}
