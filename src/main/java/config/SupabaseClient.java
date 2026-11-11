package config;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.URI;
import java.net.http.HttpResponse;
import java.io.IOException;
import repository.SupabaseException;


public class SupabaseClient {

    private static final Dotenv env = Dotenv.configure().ignoreIfMissing().load();
    private static final String SUPABASE_URL= env.get("SUPABASE_URL");
    private static final String SUPABASE_KEY= env.get("SUPABASE_KEY");
    private static final HttpClient client = HttpClient.newHttpClient();

    public HttpRequest.Builder request(String path, String accessToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();

        builder.uri(URI.create(SUPABASE_URL + path));
        builder.header("apikey", SUPABASE_KEY);
        if (accessToken != null && accessToken.isBlank() != true){
            builder.header("Authorization", "Bearer " + accessToken);
        }
        return builder;
    }
    public String send(HttpRequest request) throws SupabaseException {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() >= 200 && response.statusCode() < 300){
            return response.body();
            }
            throw new SupabaseException(response.statusCode(), response.body());

        } catch (RuntimeException e) {
                throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
