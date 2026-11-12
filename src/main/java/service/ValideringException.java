package service;

/** Kastes av en service når dataene ikke følger reglene. Controlleren gjør den til 400. */
public class ValideringException extends RuntimeException {
    public ValideringException(String melding) {
        super(melding);
    }
}
