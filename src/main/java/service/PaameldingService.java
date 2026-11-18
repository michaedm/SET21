package service;

import model.Paamelding;
import repository.PaameldingRepository;

public class PaameldingService {

    private final PaameldingRepository repository;

    public PaameldingService(PaameldingRepository repository) {
        this.repository = repository;
    }

    public void opprett(
        int kursId,
        String fornavn,
        String etternavn,
        String epost,
        String telefon
    ) throws Exception {

        if (kursId <= 0) {
            throw new IllegalArgumentException("Du må velge et kurs.");
        }

        if (fornavn == null || fornavn.isBlank()
                || etternavn == null || etternavn.isBlank()) {
            throw new IllegalArgumentException("Fornavn og etternavn er påkrevd.");
        }

        if (epost == null || epost.isBlank()) {
            throw new IllegalArgumentException("E-post er påkrevd.");
        }

        String navn = fornavn.trim() + " " + etternavn.trim();

        Paamelding paamelding = new Paamelding(
            kursId,
            navn,
            epost.trim(),
            telefon,
            "påmeldt"
        );

        repository.opprett(paamelding);
    }
}