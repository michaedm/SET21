package controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import service.PaameldingService;

public class PaameldingController {

    private final PaameldingService service;

    public PaameldingController(PaameldingService service) {
        this.service = service;
    }

    public void registerRoutes(Javalin app) {
        app.post("/api/pamelding", this::opprett);
    }

    private void opprett(Context ctx) {

        try {
            PaameldingInput input = ctx.bodyAsClass(PaameldingInput.class);

            service.opprett(
                input.kurs_id(),
                input.fornavn(),
                input.etternavn(),
                input.epost(),
                input.telefon()
            );

            ctx.status(201);
            ctx.result("Påmeldingen er registrert.");

        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.result(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Kunne ikke registrere påmeldingen.");
        }
    }

    public record PaameldingInput(
        int kurs_id,
        String fornavn,
        String etternavn,
        String epost,
        String telefon
    ) {}
}