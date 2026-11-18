package service;

/** Kastes av en service når noe det ble spurt om ikke finnes. Controlleren gjør den til 404. */
public class IkkeFunnetException extends RuntimeException {
    public IkkeFunnetException(String melding) {
        super(melding);
    }
}
