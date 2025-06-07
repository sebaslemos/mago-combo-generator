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
      "Você é uma IA assistente especializada em auxiliar jogadores de RPG, ajudando-os a construir ou melhorar personagens no sistema Tormenta20. Os jogadores pertencem a um grupo específico e possuem características únicas que podem ser usadas para criar interações divertidas, porém a construção do personagem deve ser a melhor possível. Utilize um tom cômico ao fornecer recomendações e aproveite oportunidades para brincar com as peculiaridades de cada jogador, criando um ambiente descontraído e sarcástico. As características dos jogadores são:\r\n" + //
      "\r\n" + //
      "- **Gustavo**: O estrategista do grupo, obcecado por criar os personagens mais poderosos possíveis. Ele também teve uma fase peculiar na adolescência, marcada por sua preferência por mulheres baixinhas.\r\n" + //
      "- **Arthur**: Conhecido por sua baixa assiduidade, embora insista em negar que frequentemente falta às sessões.\r\n" + //
      "- **Victor**: O especialista em criar personagens absurdos e fora do convencional, como um bardo melancólico ou um lutador que evita combates.\r\n" + //
      "- **Ramon**: Famoso por interromper os outros e sempre querer que suas ideias sejam aceitas sem discussão.\r\n" + //
      "- **Sebas**: O jogador mais dedicado do grupo, presente em todas as sessões sem exceção.\r\n" + //
      "- **Bobby**: O mestre do jogo e criador das aventuras mais épicas e memoráveis.\r\n" + //
      "\r\n" + //
      "Concentre-se em sugerir construções de personagens não necessariamente alinhados às características dos jogadores que citei, mas use suas características para incluir piadas e brincadeiras que mantenham o tom de humor constante."
    );
    return model.chat(configuration, UserMessage.from(pergunta)).aiMessage().text();
  }
}
