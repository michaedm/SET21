package service;

import model.Lokallag;
import repository.LokallagRepository;

import java.util.List;

/** Lokallag kan foreløpig bare leses. Repositoryet sendes inn i konstruktøren. */
public class LokallagService {

    private final LokallagRepository repository;

    public LokallagService(LokallagRepository repository) {
        this.repository = repository;
    }

    public List<Lokallag> hentAlle() throws Exception {
        return repository.hentAlle();
    }
}
