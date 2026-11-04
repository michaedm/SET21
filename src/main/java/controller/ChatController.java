package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.ChatInput;
import repository.OllamaRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API for chatboten nederst til høyre på nettsiden.
 *
 *   POST /api/chat   { "melding": "..." }  ->  { "svar": "..." }   (offentlig)
 *
 * Meldingen sendes videre til språkmodellen (se OllamaRepository).
 */
public class ChatController {

    private static final OllamaRepository repository = new OllamaRepository();
    private static final ObjectMapper mapper = new ObjectMapper();

    // Enkel grense: maks antall spørsmål per IP-adresse per minutt
    private static final int MAKS_PER_MINUTT = 10;
    private static final Map<String, int[]> teller = new ConcurrentHashMap<>();

    public static void registerRoutes(Javalin app) {
        app.post("/api/chat", ChatController::chat);
    }

    private static void chat(Context ctx) {
        if (forMangeSporsmal(ctx.ip())) {
            ctx.status(429).json(Map.of("feil", "Du har stilt mange spørsmål. Vent litt og prøv igjen."));
            return;
        }

        ChatInput input;
        try {
            input = mapper.readValue(ctx.body(), ChatInput.class);
        } catch (JsonProcessingException e) {
            ctx.status(400).json(Map.of("feil", "Ugyldig JSON i forespørselen"));
            return;
        }
        if (input == null) {
            ctx.status(400).json(Map.of("feil", "Forespørselen mangler innhold"));
            return;
        }

        List<String> feil = input.valider();
        if (!feil.isEmpty()) {
            ctx.status(400).json(Map.of("feil", String.join(", ", feil)));
            return;
        }

        try {
            String svar = repository.spor(input.melding().trim());
            ctx.json(Map.of("svar", svar));
        } catch (Exception e) {
            System.err.println("[ChatController] " + e);
            ctx.status(503).json(Map.of("feil", "Chatboten er ikke tilgjengelig akkurat nå. Prøv igjen om litt."));
        }
    }

    /** Teller spørsmål per IP i vinduer på ett minutt. */
    private static boolean forMangeSporsmal(String ip) {
        int minutt = (int) (System.currentTimeMillis() / 60_000);
        int[] t = teller.compute(ip, (k, v) -> (v == null || v[0] != minutt) ? new int[]{minutt, 0} : v);
        synchronized (t) {
            t[1]++;
            return t[1] > MAKS_PER_MINUTT;
        }
    }
}
