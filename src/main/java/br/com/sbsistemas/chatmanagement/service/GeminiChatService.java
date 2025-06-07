package br.com.sbsistemas.chatmanagement.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;

@BrowserCallable
@AnonymousAllowed
public class GeminiChatService {

  private final ChatModel model;

  public GeminiChatService(
    @Value("${langchain4j.google-ai.api-key}") String apiKey,
    @Value("${langchain4j.google-ai.model-name}") String modelName
  ) {
    this.model = GoogleAiGeminiChatModel.builder().apiKey(apiKey).modelName(modelName).build();
  }

  public String conversar(String pergunta) {
    SystemMessage configuration = SystemMessage.from(
      """
         Você é uma IA assistente especializada em auxiliar jogadores de RPG,
      ajudando-os a construir ou melhorar personagens no sistema Tormenta20, edição jogo do ano.
      Você deve fornecer respostas detalhadas e úteis, sempre considerando as regras do sistema Tormenta20.
      Se a pergunta não estiver relacionada ao RPG ou ao sistema Tormenta20,
      responda que não pode ajudar com isso e sugira que o usuário faça uma pergunta relacionada ao RPG.

      Vocês informará a ficha do personagem em duas partes:
      1. A primeira parte conterá as informações do personagem com uma breve explicação de cada atributo,
      bem como o porque de cada escolha.
      2. A segunda parte conterá apenas a ficha do personagem, em formato markdown,
      com os atributos, perícias, magias e equipamentos, sem explicações adicionais.

      separe as partes com um "---" e não inclua mais nada além disso.

      """
    );
    return model.chat(configuration, UserMessage.from(pergunta)).aiMessage().text();
  }
}
