package service;

import model.Kurs;
import repository.KursRepository;

import java.util.List;

/** Reglene for kurs. Repositoryet sendes inn i konstruktøren (dependency injection). */
public class KursService {

    private final KursRepository repository;

    public KursService(KursRepository repository) {
        this.repository = repository;
    }

    public List<Kurs> hentAlle() throws Exception {
        return repository.hentAlle();
    }

    public Kurs opprett(Kurs kurs, String token) throws Exception {
        if (kurs == null) {
            throw new ValideringException("Forespørselen mangler innhold");
        }
        if (kurs.tittel() == null || kurs.tittel().isBlank()) {
            throw new ValideringException("Tittel er påkrevd");
        }
        return repository.opprett(kurs, token);
    }
}
