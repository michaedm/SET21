package basictests;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.GradioRepository;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Tester GradioRepository mot et falskt Kari-Space, så testene ikke trenger Hugging Face.
 */
public class GradioTests {

    private static HttpServer fakeSpace;
    private static GradioRepository repository;

    private static String strom;
    private static String sistePost;

    @BeforeAll
    public static void start() throws Exception {
        fakeSpace = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        fakeSpace.createContext("/gradio_api/call/svar", exchange -> {
            String svar;
            if (exchange.getRequestMethod().equals("POST")) {
                sistePost = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                svar = "{\"event_id\": \"abc123\"}";
            } else {
                Assertions.assertTrue(exchange.getRequestURI().getPath().endsWith("/abc123"));
                svar = strom;
            }
            byte[] body = svar.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        fakeSpace.start();
        repository = new GradioRepository("http://localhost:" + fakeSpace.getAddress().getPort() + "/", "");
    }

    @AfterAll
    public static void stop() {
        fakeSpace.stop(0);
    }

    @BeforeEach
    public void reset() {
        sistePost = null;
        strom = "event: heartbeat\ndata: null\n\nevent: complete\ndata: [\"Jeg heter Kari.\"]\n\n";
    }

    @Test
    @DisplayName("Leser svaret fra Gradio-strømmen og sender systeminstruks + melding")
    public void readsAnswer() throws Exception {
        String svar = repository.spor("Hva heter du?");

        Assertions.assertEquals("Jeg heter Kari.", svar);
        Assertions.assertTrue(sistePost.contains("Hva heter du?"));
        Assertions.assertTrue(sistePost.contains("Du heter Kari"), "systeminstruksen skal sendes med");
    }

    @Test
    @DisplayName("Feil fra Spacet gir exception")
    public void errorEventThrows() {
        strom = "event: error\ndata: null\n\n";

        Assertions.assertThrows(IllegalStateException.class, () -> repository.spor("Hei"));
    }
}
