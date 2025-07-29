package br.com.sbsistemas.chatmanagement.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@BrowserCallable
@AnonymousAllowed
@Service
public class RedisTestService {

  @Autowired
  private JedisPool jedisPool;

  @Autowired
  private EmbeddingStore<TextSegment> embeddingStore;

  public String testarRedis() {
    StringBuilder result = new StringBuilder();

    try (Jedis jedis = jedisPool.getResource()) {
      // Teste básico de conexão
      result.append("=== TESTE DE CONEXÃO REDIS ===\n");
      result.append("Ping: ").append(jedis.ping()).append("\n");

      // Lista todas as chaves relacionadas ao projeto
      result.append("\n=== CHAVES DO PROJETO ===\n");
      Set<String> keys = jedis.keys("mago_combo*");
      result.append("Número de chaves encontradas: ").append(keys.size()).append("\n");

      for (String key : keys) {
        result.append("Chave: ").append(key).append(" - Tipo: ").append(jedis.type(key)).append("\n");
      }

      // Informações sobre o embedding store
      result.append("\n=== EMBEDDING STORE ===\n");
      result.append("Tipo do store: ").append(embeddingStore.getClass().getSimpleName()).append("\n");

      return result.toString();
    } catch (Exception e) {
      return "Erro ao testar Redis: " + e.getMessage();
    }
  }

  public String limparDados() {
    try (Jedis jedis = jedisPool.getResource()) {
      Set<String> keys = jedis.keys("mago_combo*");
      if (!keys.isEmpty()) {
        jedis.del(keys.toArray(new String[0]));
        return "Limpeza concluída. " + keys.size() + " chaves removidas.";
      } else {
        return "Nenhuma chave encontrada para limpar.";
      }
    } catch (Exception e) {
      return "Erro ao limpar dados: " + e.getMessage();
    }
  }
}
