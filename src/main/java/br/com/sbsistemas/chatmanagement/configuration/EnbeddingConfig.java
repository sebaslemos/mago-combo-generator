package br.com.sbsistemas.chatmanagement.configuration;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiTokenCountEstimator;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class EnbeddingConfig {

	@Bean
	public EmbeddingStore<TextSegment> manualBasicoEmbeddingStore(EmbeddingModel embeddingModel) {
		try {
			RedisEmbeddingStore store = RedisEmbeddingStore.builder()
					.host("localhost")
					.port(6379)
					.dimension(embeddingModel.dimension())
					.prefix("mago_combo:")
					.indexName("mago_combo_index")
					.build();

			return store;
		} catch (Exception e) {
			// Fallback para InMemoryEmbeddingStore em caso de erro
			return new InMemoryEmbeddingStore<TextSegment>();
		}
	}

	@Bean
	public EmbeddingStoreIngestor embeddingStoreIngestor(
			EmbeddingModel embeddingModel,
			EmbeddingStore<TextSegment> embeddingStore,
			GoogleAiGeminiTokenCountEstimator tokenCountEstimator) {
		return EmbeddingStoreIngestor.builder()
				.embeddingModel(embeddingModel)
				.embeddingStore(embeddingStore)
				.documentSplitter(DocumentSplitters.recursive(10000, 1000))
				.build();
	}

	@Bean
	public EmbeddingStoreContentRetriever embeddingRetriever(
			EmbeddingModel embeddingModel,
			EmbeddingStore<TextSegment> embeddingStore) {
		EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
				.embeddingStore(embeddingStore)
				.embeddingModel(embeddingModel)
				.minScore(0.8)
				.maxResults(20)
				.build();

		return retriever;
	}

	@Bean(destroyMethod = "close")
	public JedisPool jedisPool() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setJmxEnabled(false); // Disable JMX to avoid conflicts
		config.setMaxTotal(20); // Aumenta o pool para mais conexões
		config.setMaxIdle(10); // Aumenta o número de conexões idle
		config.setMinIdle(2); // Mantém pelo menos 2 conexões ativas
		config.setTestOnBorrow(true); // Testa conexões antes de usar
		config.setTestOnReturn(true); // Testa conexões quando retornadas
		config.setTestWhileIdle(true); // Testa conexões idle
		return new JedisPool(config, "localhost", 6379);
	}
}
