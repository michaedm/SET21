package repository;
import config.SupabaseClient;

import model.Kurs;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.List;


public class KursRepository {

    private final SupabaseClient supabaseClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public KursRepository(SupabaseClient supabaseClient) {
        this.supabaseClient = supabaseClient;
    }

    public List<Kurs> hentAlle() throws Exception {

        HttpRequest request = supabaseClient.request("/rest/v1/kurs?select=*&order=startdato.asc", null)
                .GET()
                .build();

        String response = supabaseClient.send(request);


        System.out.println("Supabase response: " + response);
        
        return mapper.readValue(
                response,
                new TypeReference<List<Kurs>>() {}
        );
    }
    public Kurs opprett(Kurs kurs, String accessToken) throws Exception {


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

    HttpRequest request = supabaseClient.request("/rest/v1/kurs", accessToken)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    String response = supabaseClient.send(request);


    List<Kurs> lagret = mapper.readValue(
            response,
            new TypeReference<List<Kurs>>() {}
    );

    return lagret.get(0);
    }
}