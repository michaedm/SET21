package repository;

import model.Kurs;
import io.github.cdimascio.dotenv.Dotenv;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class KursRepository {

    private final Dotenv env = Dotenv.configure().ignoreIfMissing().load();
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Kurs> hentAlle() throws Exception {

        String supabaseUrl = env.get("SUPABASE_URL");
        String supabaseKey = env.get("SUPABASE_KEY");

        String apiUrl = supabaseUrl + "/rest/v1/kurs?select=*&order=startdato.asc";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        System.out.println("Supabase status: " + response.statusCode());
        System.out.println("Supabase response: " + response.body());
        
        if (response.statusCode() != 200) {
                throw new RuntimeException("Supabase GET failed: HTTP " + response.statusCode());
        }
        return mapper.readValue(
                response.body(),
                new TypeReference<List<Kurs>>() {}
        );
    }
    public Kurs opprett(Kurs kurs, String accessToken) throws Exception {

    String supabaseUrl = env.get("SUPABASE_URL");
    String supabaseKey = env.get("SUPABASE_KEY");

    String apiUrl = supabaseUrl + "/rest/v1/kurs";

    java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();

    data.put("tittel", kurs.tittel());
    data.put("tema", kurs.tema());
    data.put("beskrivelse", kurs.beskrivelse());
    data.put("sted", kurs.sted());
    data.put("startdato", kurs.startdato());
    data.put("sluttdato", kurs.sluttdato());
    data.put("maks_deltakere", kurs.maksDeltakere());
    data.put("pris", kurs.pris());
    data.put("status", kurs.status());
    data.put("lokallag_id", kurs.lokallagId());

    String json = mapper.writeValueAsString(data);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("apikey", supabaseKey)
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString()
            
    );

    if (response.statusCode() != 201) {
        throw new RuntimeException(
                "Kunne ikke lagre kurs: " + response.body()
        );
    }

    List<Kurs> lagret = mapper.readValue(
            response.body(),
            new TypeReference<List<Kurs>>() {}
    );

    return lagret.get(0);
    }
}