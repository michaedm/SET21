package repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Snakker med språkmodellen (Ollama) som kjører i en egen container.
 *
 * Adressen kommer fra OLLAMA_URL:
 *   lokalt:          http://localhost:7860
 *   Docker Compose:  http://ollama:7860
 *   Hugging Face:    https://<bruker>-set21-ollama.hf.space (sammen med HF_TOKEN)
 */
public class OllamaRepository implements ChatModell {

    private final Dotenv env = Dotenv.configure().ignoreIfMissing().load();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String spor(String melding) throws Exception {
        String ollamaUrl = env.get("OLLAMA_URL", "http://localhost:7860");
        String modell = env.get("OLLAMA_MODEL", "gemma3:1b");
        String token = env.get("HF_TOKEN", "");

        // Begrenser lengden på svaret, så det ikke tar for lang tid
        Map<String, Object> options = new HashMap<>();
        options.put("num_predict", 200);
        String traader = env.get("OLLAMA_NUM_THREAD", "");
        if (!traader.isBlank()) {
            options.put("num_thread", Integer.parseInt(traader.trim()));
        }

        Map<String, Object> body = Map.of(
                "model", modell,
                "stream", false,
                "options", options,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEMINSTRUKS),
                        Map.of("role", "user", "content", melding)
                )
        );

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(ollamaUrl + "/api/chat"))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        if (!token.isBlank()) {
            request.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Ollama svarte " + response.statusCode() + ": " + response.body());
        }

        JsonNode svar = mapper.readTree(response.body()).path("message").path("content");
        if (svar.isMissingNode() || svar.asText().isBlank()) {
            throw new IllegalStateException("Ollama svarte uten tekst: " + response.body());
        }
        return svar.asText().trim();
    }
}
