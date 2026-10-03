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

    private final Dotenv env = Dotenv.load();
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

        return mapper.readValue(
                response.body(),
                new TypeReference<List<Kurs>>() {}
        );
    }
}