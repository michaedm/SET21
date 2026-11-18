package repository;

import model.Lokallag;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.List;
import config.SupabaseClient;

public class LokallagRepository {


    private final ObjectMapper mapper = new ObjectMapper();
    private final SupabaseClient supabaseClient;
    public LokallagRepository(SupabaseClient supabaseClient) {
        this.supabaseClient = supabaseClient;
    }

    public List<Lokallag> hentAlle() throws Exception {

        HttpRequest request = supabaseClient.request(
                "/rest/v1/lokallag?select=*&order=navn.asc", null)
                .GET()
                .build();

        String json = supabaseClient.send(request);

        List<Lokallag> lokallag = mapper.readValue(
                json,
                new TypeReference<List<Lokallag>>() {}
        );

        return lokallag;
    }
}