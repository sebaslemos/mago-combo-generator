package br.com.sbsistemas.chatmanagement.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@BrowserCallable
@AnonymousAllowed
@Service
public class MagoCombeiroChatService {

	@Autowired
	private AiService aiChatService;

	@Autowired
	private EmbeddingStoreIngestor ingestor;

	@Autowired
	private JedisPool jedisPool;

	private static final String MANUAL_PERSONAGENS_INGESTION_FLAG = "mago_combo_document_ingested_manual_personagens";

	// private static final String DOCUMENT_HASH_KEY =
	// "mago_combo:document_hash:portaria";

	public MagoCombeiroChatService() {
	}

	public String conversar(String pergunta) throws IOException {
		String result = aiChatService.chat(pergunta);
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
		// clearDocument();
		try (Jedis jedis = jedisPool.getResource()) {
			if (!isDocumentIngested()) {
				URL fileUrl = MagoCombeiroChatService.class.getClassLoader().getResource("T20-regras.pdf");
				if (fileUrl == null) {
					throw new RuntimeException("Arquivo não encontrado no classpath");
				}
				Path filePath = Paths.get(fileUrl.toURI());
				Document document = FileSystemDocumentLoader.loadDocument(filePath, new ApachePdfBoxDocumentParser());

				System.out.println("Documento carregado. Tamanho: " + document.text().length() + " caracteres");
				// ...existing code...
				IngestionResult ingest = ingestor.ingest(document);
				System.out.println("Ingestão concluída.");

				// Marca como processado
				jedis.set(MANUAL_PERSONAGENS_INGESTION_FLAG, "true");
			} else {
				System.out.println("Documento já foi ingerido anteriormente.");
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException("Erro ao carregar o documento", e);
		}
	}
}
