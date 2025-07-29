package br.com.sbsistemas.chatmanagement.configuration;

import br.com.sbsistemas.chatmanagement.service.AiService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiTokenCountEstimator;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

  @Value("${langchain4j.google-ai.api-key}")
  String apiKey;

  @Value("${langchain4j.google-ai.model-name}")
  String modelName;

  @Bean
  public ChatModel geminiChatModel() {
    return GoogleAiGeminiChatModel.builder().apiKey(apiKey).modelName(modelName).build();
  }

  @Bean
  public EmbeddingModel googleAiEmbeddingModel() {
    return GoogleAiEmbeddingModel.builder().apiKey(apiKey).modelName("embedding-001").build();
  }

  @Bean
  public GoogleAiGeminiTokenCountEstimator googleAiGeminiTokenCountEstimator() {
    return GoogleAiGeminiTokenCountEstimator.builder().apiKey(apiKey).modelName(modelName).build();
  }

  @Bean
  public ChatMemoryProvider chatMemoryProvider() {
    return chatId -> MessageWindowChatMemory.withMaxMessages(4);
  }

  @Bean
  public AiService googleAiService(
    ChatModel model,
    EmbeddingStoreContentRetriever contentRetriever,
    ChatMemoryProvider chatMemoryProvider
  ) {
    return AiServices.builder(AiService.class)
      .chatModel(model)
      .contentRetriever(contentRetriever)
      .chatMemoryProvider(chatMemoryProvider)
      .build();
  }
}
