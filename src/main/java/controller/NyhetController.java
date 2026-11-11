package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import controller.NyhetController.ApiFeil;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.Nyhet;
import model.NyhetInput;
import repository.NyhetRepository;
import repository.SupabaseException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * API for nyheter (bloggliknende publisering).
 *
 *   GET    /api/nyheter            alle nyheter, nyeste først   (offentlig)
 *   GET    /api/nyheter/{id}       én nyhet                     (offentlig)
 *   POST   /api/nyheter            publiser ny nyhet            (innlogget redaktør)
 *   PUT    /api/nyheter/{id}       endre nyhet                  (innlogget redaktør)
 *   DELETE /api/nyheter/{id}       slett nyhet                  (innlogget redaktør)
 *
 * Redaktøren logger inn med Supabase Auth i nettleseren og sender tokenet sitt i
 * headeren "Authorization: Bearer <access_token>". Se docs/api.md.
 */
public class NyhetController {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ZoneId NORGE = ZoneId.of("Europe/Oslo");
    private static final int MAKS_ANTALL = 100;
    private final NyhetRepository repository;

    public NyhetController(NyhetRepository repository) {
        this.repository = repository;
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/nyheter", this::hentAlle);
        app.get("/api/nyheter/{id}", this::hentEn);
        app.post("/api/nyheter", this::opprett);
        app.put("/api/nyheter/{id}", this::oppdater);
        app.delete("/api/nyheter/{id}", this::slett);

        app.exception(ApiFeil.class, (e, ctx) ->
                ctx.status(e.status).json(Map.of("feil", e.getMessage())));

        app.exception(SupabaseException.class, (e, ctx) -> {
            int status = switch (e.status()) {
                case 401 -> 401;            // ugyldig eller utløpt innlogging
                case 403 -> 403;            // innlogget, men RLS sier nei
                case 409 -> 400;            // f.eks. lokallag_id som ikke finnes
                default -> 500;
            };
            String melding = switch (status) {
                case 401 -> "Du må logge inn på nytt";
                case 403 -> "Du har ikke tilgang til å gjøre dette";
                case 400 -> "Ugyldige data (finnes lokallaget?)";
                default -> "Noe gikk galt mot databasen";
            };
            System.err.println("[NyhetController] " + e.getMessage());
            ctx.status(status).json(Map.of("feil", melding));
        });
    }

    // ---------- Handlere ----------

    private void hentAlle(Context ctx) throws Exception {
        Integer lokallagId = valgfrittTall(ctx.queryParam("lokallag_id"), "lokallag_id");
        Integer antall = valgfrittTall(ctx.queryParam("antall"), "antall");
        if (antall != null && antall > MAKS_ANTALL) {
            antall = MAKS_ANTALL;
        }

        List<Nyhet> nyheter = repository.hentAlle(lokallagId, antall);
        ctx.json(nyheter);
    }

    private void hentEn(Context ctx) throws Exception {
        int id = nyhetId(ctx);
        Nyhet nyhet = repository.hentEn(id)
                .orElseThrow(() -> new ApiFeil(404, "Fant ingen nyhet med id " + id));
        ctx.json(nyhet);
    }

    private void opprett(Context ctx) throws Exception {
        String token = krevInnlogging(ctx);
        NyhetInput nyhet = lesOgValider(ctx);

        Nyhet lagret = repository.opprett(nyhet, token);
        ctx.status(201).json(lagret);
    }

    private void oppdater(Context ctx) throws Exception {
        String token = krevInnlogging(ctx);
        int id = nyhetId(ctx);
        NyhetInput nyhet = lesOgValider(ctx);

        Nyhet endret = repository.oppdater(id, nyhet, token)
                .orElseThrow(() -> new ApiFeil(404, "Fant ingen nyhet med id " + id));
        ctx.json(endret);
    }

    private void slett(Context ctx) throws Exception {
        String token = krevInnlogging(ctx);
        int id = nyhetId(ctx);

        if (!repository.slett(id, token)) {
            throw new ApiFeil(404, "Fant ingen nyhet med id " + id);
        }
        ctx.status(204);
    }

    // ---------- Hjelpemetoder ----------

    /** Henter tokenet fra "Authorization: Bearer ...", ellers 401. */
    private static String krevInnlogging(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ApiFeil(401, "Du må være logget inn som redaktør");
        }
        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            throw new ApiFeil(401, "Du må være logget inn som redaktør");
        }
        return token;
    }

    private static NyhetInput lesOgValider(Context ctx) {
        NyhetInput nyhet;
        try {
            nyhet = mapper.readValue(ctx.body(), NyhetInput.class);
        } catch (JsonProcessingException e) {
            throw new ApiFeil(400, "Ugyldig JSON i forespørselen");
        }
        if (nyhet == null) {
            throw new ApiFeil(400, "Forespørselen mangler innhold");
        }

        List<String> feil = nyhet.valider();
        if (!feil.isEmpty()) {
            throw new ApiFeil(400, String.join(", ", feil));
        }
        return nyhet.klargjort(LocalDate.now(NORGE));
    }

    private static int nyhetId(Context ctx) {
        Integer id = valgfrittTall(ctx.pathParam("id"), "id");
        if (id == null || id <= 0) {
            throw new ApiFeil(400, "id må være et positivt tall");
        }
        return id;
    }

    private static Integer valgfrittTall(String verdi, String navn) {
        if (verdi == null || verdi.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(verdi.trim());
        } catch (NumberFormatException e) {
            throw new ApiFeil(400, navn + " må være et tall");
        }
    }

    /** Feil vi selv oppdager (400, 401, 404). Blir til JSON: {"feil": "..."} */
    static class ApiFeil extends RuntimeException {
        final int status;

        ApiFeil(int status, String melding) {
            super(melding);
            this.status = status;
        }
    }
}
