package br.com.sbsistemas.chatmanagement.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.sbsistemas.chatmanagement.service.AiService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;

@Configuration
public class OpenAiConfig {

    @Value("${langchain4j.openai.api-key}")
    String apiKey;

    @Value("${langchain4j.openai.model-name}")
    String modelName;

    @Bean
    @Qualifier("openAiChatModel")
    public ChatModel openAiChatModel() {
        return OpenAiChatModel.builder().apiKey(apiKey).modelName(modelName).build();
    }

    @Bean
    @Qualifier("openAiEmbeddingModel")
    public EmbeddingModel openAiEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    @Qualifier("openAiService")
    public AiService openAiService(
            @Qualifier("openAiChatModel") ChatModel model,
            EmbeddingStoreContentRetriever contentRetriever,
            ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(AiService.class)
                .chatModel(model)
                .contentRetriever(contentRetriever)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }
}
