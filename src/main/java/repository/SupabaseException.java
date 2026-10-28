package repository;

/**
 * Kastes når Supabase svarer med en feilkode (alt som ikke er 2xx).
 * Controlleren bruker statuskoden til å gi et passende svar til nettsiden.
 */
public class SupabaseException extends Exception {

    private final int status;
    private final String body;

    public SupabaseException(int status, String body) {
        super("Supabase svarte " + status + ": " + body);
        this.status = status;
        this.body = body;
    }

    public int status() {
        return status;
    }

    public String body() {
        return body;
    }
}
