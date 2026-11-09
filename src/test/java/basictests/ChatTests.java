package basictests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import controller.ChatController;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Tester for POST /api/chat med en falsk språkmodell på localhost:18322 (satt i pom.xml),
 * så testene ikke trenger Ollama-containeren.
 */
public class ChatTests {

    private static HttpServer fakeOllama;
    private static Javalin app;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static int ollamaStatus;
    private static String ollamaBody;
    private static String sisteForesporsel;

    @BeforeAll
    public static void startServers() throws Exception {
        fakeOllama = HttpServer.create(new InetSocketAddress("localhost", 18322), 0);
        fakeOllama.createContext("/", exchange -> {
            sisteForesporsel = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] body = ollamaBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(ollamaStatus, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        fakeOllama.start();

        app = Javalin.create().start(0);
        ChatController.registerRoutes(app);
    }

    @AfterAll
    public static void stopServers() {
        app.stop();
        fakeOllama.stop(0);
    }

    @BeforeEach
    public void reset() {
        ollamaStatus = 200;
        ollamaBody = """
                {"message": {"role": "assistant", "content": "Husflid er tradisjonelt håndverk."}}
                """;
        sisteForesporsel = null;
    }

    private HttpResponse<String> post(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("POST /api/chat svarer med teksten fra modellen")
    public void chatReturnsAnswer() throws Exception {
        HttpResponse<String> response = post("{\"melding\": \"Hva er husflid?\"}");

        Assertions.assertEquals(200, response.statusCode());
        JsonNode svar = mapper.readTree(response.body());
        Assertions.assertEquals("Husflid er tradisjonelt håndverk.", svar.get("svar").asText());
        Assertions.assertTrue(sisteForesporsel.contains("Hva er husflid?"));
        Assertions.assertTrue(sisteForesporsel.contains("Østfold husflid"), "systeminstruksen skal sendes med");
    }

    @Test
    @DisplayName("Tom melding gir 400")
    public void emptyMessageIsRejected() throws Exception {
        HttpResponse<String> response = post("{\"melding\": \"   \"}");

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertNull(sisteForesporsel, "modellen skal ikke spørres");
    }

    @Test
    @DisplayName("For lang melding gir 400")
    public void tooLongMessageIsRejected() throws Exception {
        HttpResponse<String> response = post("{\"melding\": \"" + "a".repeat(501) + "\"}");

        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("Ugyldig JSON gir 400")
    public void invalidJsonIsRejected() throws Exception {
        HttpResponse<String> response = post("ikke json");

        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("Feil fra modellen gir 503 med norsk feilmelding")
    public void modelErrorGives503() throws Exception {
        ollamaStatus = 500;
        ollamaBody = "{\"error\": \"noe gikk galt\"}";

        HttpResponse<String> response = post("{\"melding\": \"Hei\"}");

        Assertions.assertEquals(503, response.statusCode());
        Assertions.assertTrue(mapper.readTree(response.body()).has("feil"));
    }
}
