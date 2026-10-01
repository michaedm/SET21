package repository;

import model.Nyhet;
import io.github.cdimascio.dotenv.Dotenv;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class NyhetRepository {

    private final Dotenv env = Dotenv.load();
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Nyhet> hentAlle() throws Exception {

        String supabaseUrl = env.get("SUPABASE_URL");
        String supabaseKey = env.get("SUPABASE_KEY");

        String apiUrl = supabaseUrl + "/rest/v1/publish?select=*";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        String json = response.body();

        List<Nyhet> nyheter = mapper.readValue(
                json,
                new TypeReference<List<Nyhet>>() {}
        );

        return nyheter;
    }
}