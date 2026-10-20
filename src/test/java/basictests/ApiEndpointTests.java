package basictests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.KursController;
import controller.LokallagController;
import controller.NyhetController;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Integrasjonstester for API-et: ekte Javalin-server + ekte controllere + ekte repositories,
 * men med FakeSupabase i stedet for den ekte databasen.
 * Dette er de samme endepunktene som nettsiden (script.js) kaller.
 */
public class ApiEndpointTests {

    private static FakeSupabase supabase;
    private static Javalin app;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void startServers() throws Exception {
        supabase = new FakeSupabase();
        supabase.start();

        // Samme ruter som App.java, men på en ledig port (0) så den ikke kolliderer med 7070
        app = Javalin.create().start(0);
        NyhetController.registerRoutes(app);
        LokallagController.registerRoutes(app);
        KursController.registerRoutes(app);
    }

    @AfterAll
    public static void stopServers() {
        app.stop();
        supabase.stop();
    }

    @BeforeEach
    public void resetFakeSupabase() {
        supabase.reset();
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("GET /api/nyheter svarer 200 med feltene nettsiden trenger")
    public void nyheterEndpointReturnsNews() throws Exception {
        supabase.respondWith(200, """
                [{"nyhet_id": 1, "tittel": "Test", "innhold": "Dette er en test",
                  "bilde_url": "", "publisert_dato": "2026-09-30", "lokallag_id": 1}]
                """);

        HttpResponse<String> response = get("/api/nyheter");

        Assertions.assertEquals(200, response.statusCode());
        JsonNode first = mapper.readTree(response.body()).get(0);
        Assertions.assertEquals("Test", first.get("tittel").asText());
        Assertions.assertEquals("Dette er en test", first.get("innhold").asText());
        Assertions.assertEquals("2026-09-30", first.get("publisert_dato").asText());
        Assertions.assertTrue(first.has("bilde_url"));
    }

    @Test
    @DisplayName("GET /api/lokallag svarer 200 med en liste")
    public void lokallagEndpointReturnsList() throws Exception {
        supabase.respondWith(200, """
                [{"lokallag_id": 1, "navn": "Moss"}, {"lokallag_id": 2, "navn": "Halden"}]
                """);

        HttpResponse<String> response = get("/api/lokallag");

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(2, mapper.readTree(response.body()).size());
    }

    @Test
    @DisplayName("GET /api/kurs svarer 200 med en liste")
    public void kursEndpointReturnsList() throws Exception {
        supabase.respondWith(200, """
                [{"kurs_id": 1, "tittel": "Strikk", "tema": null, "beskrivelse": null,
                  "sted": "Moss", "startdato": "2026-11-01", "sluttdato": null,
                  "maks_deltakere": 10, "pris": 250, "status": "aktiv", "lokallag_id": 1}]
                """);

        HttpResponse<String> response = get("/api/kurs");

        Assertions.assertEquals(200, response.statusCode());
        JsonNode first = mapper.readTree(response.body()).get(0);
        Assertions.assertEquals("Strikk", first.get("tittel").asText());
        Assertions.assertEquals(10, first.get("maks_deltakere").asInt());
    }

    @Test
    @DisplayName("Ingen nyheter i databasen gir 200 og en tom liste")
    public void noNewsGivesEmptyList() throws Exception {
        supabase.respondWith(200, "[]");

        HttpResponse<String> response = get("/api/nyheter");

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(0, mapper.readTree(response.body()).size());
    }

    @Test
    @DisplayName("Hvis databasen feiler, svarer API-et 500 (serveren krasjer ikke)")
    public void databaseErrorGives500() throws Exception {
        supabase.respondWith(500, """
                {"message": "database is down"}
                """);

        HttpResponse<String> response = get("/api/nyheter");

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("Ukjent adresse gir 404")
    public void unknownPathGives404() throws Exception {
        Assertions.assertEquals(404, get("/api/finnes-ikke").statusCode());
    }
}
