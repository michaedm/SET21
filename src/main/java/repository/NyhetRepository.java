package repository;

import model.Nyhet;
import model.NyhetInput;
import io.github.cdimascio.dotenv.Dotenv;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

/**
 * Snakker med tabellen "publish" i Supabase (via REST-API-et).
 *
 * Lesing bruker bare den offentlige nøkkelen (alle kan lese nyheter).
 * Skriving (opprett/endre/slett) sender med redaktørens innloggings-token,
 * slik at Supabase sin Row Level Security bestemmer om det er lov.
 */
public class NyhetRepository {

    private static final String TABELL = "/rest/v1/publish";
    private static final String SORTERING = "order=publisert_dato.desc,nyhet_id.desc";

    private final Dotenv env = Dotenv.configure().ignoreIfMissing().load();
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- Lesing (offentlig) ----------

    /** Alle nyheter, nyeste først. */
    public List<Nyhet> hentAlle() throws Exception {
        return hentAlle(null, null);
    }

    /**
     * Nyheter, nyeste først.
     * @param lokallagId bare nyheter fra dette lokallaget (null = alle)
     * @param antall maks antall nyheter (null = alle)
     */
    public List<Nyhet> hentAlle(Integer lokallagId, Integer antall) throws Exception {
        StringBuilder query = new StringBuilder("select=*&" + SORTERING);
        if (lokallagId != null) {
            query.append("&lokallag_id=eq.").append(lokallagId);
        }
        if (antall != null) {
            query.append("&limit=").append(antall);
        }

        HttpRequest request = request(TABELL + "?" + query, null).GET().build();
        return lesListe(send(request));
    }

    /** Én nyhet, eller tom hvis den ikke finnes. */
    public Optional<Nyhet> hentEn(int nyhetId) throws Exception {
        HttpRequest request = request(TABELL + "?select=*&nyhet_id=eq." + nyhetId, null)
                .GET()
                .build();
        return lesListe(send(request)).stream().findFirst();
    }

    // ---------- Skriving (krever innlogget redaktør) ----------

    /** Lagrer en ny nyhet og returnerer den slik den ble lagret (med nyhet_id). */
    public Nyhet opprett(NyhetInput nyhet, String token) throws Exception {
        HttpRequest request = request(TABELL, token)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(nyhet)))
                .build();

        List<Nyhet> lagret = lesListe(send(request));
        if (lagret.isEmpty()) {
            // Skjer hvis Supabase lagret, men ikke lot oss lese raden tilbake
            throw new SupabaseException(500, "Supabase returnerte ingen rad etter lagring");
        }
        return lagret.get(0);
    }

    /** Endrer en nyhet. Tom hvis nyheten ikke finnes. */
    public Optional<Nyhet> oppdater(int nyhetId, NyhetInput nyhet, String token) throws Exception {
        HttpRequest request = request(TABELL + "?nyhet_id=eq." + nyhetId, token)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(nyhet)))
                .build();

        return lesListe(send(request)).stream().findFirst();
    }

    /** Sletter en nyhet. Returnerer false hvis den ikke fantes. */
    public boolean slett(int nyhetId, String token) throws Exception {
        HttpRequest request = request(TABELL + "?nyhet_id=eq." + nyhetId, token)
                .header("Prefer", "return=representation")
                .DELETE()
                .build();

        return !lesListe(send(request)).isEmpty();
    }

    // ---------- Hjelpemetoder ----------

    /**
     * Lager en request med API-nøkkel. Hvis token er satt (innlogget redaktør),
     * brukes det i Authorization slik at Supabase vet hvem som spør.
     */
    private HttpRequest.Builder request(String pathOgQuery, String token) {
        String supabaseUrl = env.get("SUPABASE_URL");
        String supabaseKey = env.get("SUPABASE_KEY");
        String bearer = (token != null) ? token : supabaseKey;

        return HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + pathOgQuery))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + bearer);
    }

    private String send(HttpRequest request) throws Exception {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new SupabaseException(status, response.body());
        }
        return response.body();
    }

    private List<Nyhet> lesListe(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return mapper.readValue(json, new TypeReference<List<Nyhet>>() {});
    }
}
