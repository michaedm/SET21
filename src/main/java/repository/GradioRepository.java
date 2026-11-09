package repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Snakker med Kari i et Gradio-Space på Hugging Face (gratis).
 *
 * Gradio-API-et tar to kall:
 *   1. POST {space}/gradio_api/call/svar  {"data": [systeminstruks, melding]}  ->  {"event_id": "..."}
 *   2. GET  {space}/gradio_api/call/svar/{event_id}  ->  strøm med "event: complete" og "data: ["svaret"]"
 *
 * Adressen kommer fra KARI_SPACE_URL, f.eks. https://ia911-kari.hf.space
 * HF_TOKEN er valgfri (trengs bare hvis Spacet er privat).
 */
public class GradioRepository implements ChatModell {

    private final String spaceUrl;
    private final String token;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public GradioRepository(String spaceUrl, String token) {
        this.spaceUrl = spaceUrl.replaceAll("/+$", "");
        this.token = token == null ? "" : token;
    }

    @Override
    public String spor(String melding) throws Exception {
        // 1. Start jobben
        String body = mapper.writeValueAsString(Map.of("data", List.of(SYSTEMINSTRUKS, melding)));
        HttpResponse<String> start = client.send(
                request(spaceUrl + "/gradio_api/call/svar")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        if (start.statusCode() < 200 || start.statusCode() >= 300) {
            throw new IllegalStateException("Kari-Spacet svarte " + start.statusCode() + ": " + start.body());
        }
        String eventId = mapper.readTree(start.body()).path("event_id").asText();
        if (eventId.isBlank()) {
            throw new IllegalStateException("Kari-Spacet ga ingen event_id: " + start.body());
        }

        // 2. Hent resultatet (Spacet kan bruke litt tid hvis det må våkne)
        HttpResponse<String> resultat = client.send(
                request(spaceUrl + "/gradio_api/call/svar/" + eventId).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (resultat.statusCode() < 200 || resultat.statusCode() >= 300) {
            throw new IllegalStateException("Kari-Spacet svarte " + resultat.statusCode() + ": " + resultat.body());
        }
        return lesSvar(resultat.body());
    }

    private HttpRequest.Builder request(String url) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(180));
        if (!token.isBlank()) {
            b.header("Authorization", "Bearer " + token);
        }
        return b;
    }

    /** Finner data-linjen etter "event: complete" i strømmen fra Gradio. */
    private String lesSvar(String strom) throws Exception {
        String hendelse = "";
        for (String linje : strom.split("\\R")) {
            if (linje.startsWith("event:")) {
                hendelse = linje.substring(6).trim();
            } else if (linje.startsWith("data:") && hendelse.equals("complete")) {
                JsonNode data = mapper.readTree(linje.substring(5).trim());
                String svar = data.path(0).asText("").trim();
                if (svar.isEmpty()) {
                    throw new IllegalStateException("Kari-Spacet svarte uten tekst");
                }
                return svar;
            } else if (hendelse.equals("error")) {
                throw new IllegalStateException("Kari-Spacet meldte feil: " + strom);
            }
        }
        throw new IllegalStateException("Fant ikke svar i strømmen fra Kari-Spacet: " + strom);
    }
}
