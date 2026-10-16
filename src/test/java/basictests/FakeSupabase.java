package basictests;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * En "Fake" (test double) som later som den er Supabase.
 * Den kjører på localhost:18321 (satt i pom.xml), svarer med JSON som testen bestemmer,
 * og husker hva repositoryet spurte etter, slik at testen kan sjekke det.
 */
public class FakeSupabase {

    private final HttpServer server;
    private String responseBody = "[]";
    private int responseStatus = 200;

    private String lastPath;
    private String lastQuery;
    private String lastApiKey;

    public FakeSupabase() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 18321), 0);
        server.createContext("/", exchange -> {
            lastPath = exchange.getRequestURI().getPath();
            lastQuery = exchange.getRequestURI().getQuery();
            lastApiKey = exchange.getRequestHeaders().getFirst("apikey");

            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    /** Bestem hva "databasen" skal svare med i neste kall. */
    public void respondWith(int status, String json) {
        this.responseStatus = status;
        this.responseBody = json;
    }

    public void reset() {
        respondWith(200, "[]");
        lastPath = null;
        lastQuery = null;
        lastApiKey = null;
    }

    public String lastPath() {
        return lastPath;
    }

    public String lastQuery() {
        return lastQuery;
    }

    public String lastApiKey() {
        return lastApiKey;
    }
}
