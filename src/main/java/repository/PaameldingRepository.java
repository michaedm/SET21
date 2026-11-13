package repository;

import config.SupabaseClient;
import model.Paamelding;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpRequest;

public class PaameldingRepository {

    private final SupabaseClient supabaseClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public PaameldingRepository(SupabaseClient supabaseClient) {
        this.supabaseClient = supabaseClient;
    }

    public void opprett(Paamelding paamelding) throws Exception {

        String json = mapper.writeValueAsString(paamelding);

        HttpRequest request = supabaseClient
            .request("/rest/v1/paamelding", null)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        supabaseClient.send(request);
    }
}