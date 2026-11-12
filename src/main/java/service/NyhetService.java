package service;

import model.Nyhet;
import model.NyhetInput;
import repository.NyhetRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Reglene for nyheter. Controlleren tar seg av HTTP, repositoryet av databasen,
 * og denne klassen avgjør hva som er lov og hvordan dataene skal se ut.
 *
 * Repositoryet (og klokka) sendes inn i konstruktøren (dependency injection),
 * slik at testene kan bytte dem ut.
 */
public class NyhetService {

    public static final int MAKS_ANTALL = 100;
    private static final ZoneId NORGE = ZoneId.of("Europe/Oslo");

    private final NyhetRepository repository;
    private final Clock klokke;

    public NyhetService(NyhetRepository repository) {
        this(repository, Clock.system(NORGE));
    }

    public NyhetService(NyhetRepository repository, Clock klokke) {
        this.repository = repository;
        this.klokke = klokke;
    }

    /** Nyeste først. antall begrenses til MAKS_ANTALL. null betyr "ingen filter/grense". */
    public List<Nyhet> hentAlle(Integer lokallagId, Integer antall) throws Exception {
        if (antall != null && antall <= 0) {
            throw new ValideringException("antall må være større enn 0");
        }
        if (antall != null && antall > MAKS_ANTALL) {
            antall = MAKS_ANTALL;
        }
        return repository.hentAlle(lokallagId, antall);
    }

    public Nyhet hentEn(int id) throws Exception {
        return repository.hentEn(id)
                .orElseThrow(() -> new IkkeFunnetException("Fant ingen nyhet med id " + id));
    }

    public Nyhet opprett(NyhetInput nyhet, String token) throws Exception {
        return repository.opprett(valider(nyhet), token);
    }

    public Nyhet oppdater(int id, NyhetInput nyhet, String token) throws Exception {
        return repository.oppdater(id, valider(nyhet), token)
                .orElseThrow(() -> new IkkeFunnetException("Fant ingen nyhet med id " + id));
    }

    public void slett(int id, String token) throws Exception {
        if (!repository.slett(id, token)) {
            throw new IkkeFunnetException("Fant ingen nyhet med id " + id);
        }
    }

    /** Sjekker reglene og rydder opp (mellomrom, tomme felt, dagens dato). */
    private NyhetInput valider(NyhetInput nyhet) {
        if (nyhet == null) {
            throw new ValideringException("Forespørselen mangler innhold");
        }
        List<String> feil = nyhet.valider();
        if (!feil.isEmpty()) {
            throw new ValideringException(String.join(", ", feil));
        }
        return nyhet.klargjort(LocalDate.now(klokke));
    }
}
