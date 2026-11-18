package controller;

import io.javalin.Javalin;
import model.Kurs;
import service.KursService;
import service.ValideringException;

public class KursController {

    private final KursService service;

    public KursController(KursService service) {
        this.service = service;
    }

    public void registerRoutes(Javalin app) {

        // Hent alle kurs
        app.get("/api/kurs", ctx -> {
            try {
                ctx.json(service.hentAlle());
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Kunne ikke hente kurs");
            }
        });

        // Opprett nytt kurs
        app.post("/api/kurs", ctx -> {
            try {
                String authorization = ctx.header("Authorization");

                if (authorization == null || !authorization.startsWith("Bearer ")) {
                    ctx.status(401).result("Du må logge inn");
                    return;
                }

                String accessToken = authorization.substring("Bearer ".length()).trim();

                if (accessToken.isEmpty()) {
                    ctx.status(401).result("Ugyldig innlogging");
                    return;
                }

                Kurs nyttKurs = ctx.bodyAsClass(Kurs.class);
                Kurs lagretKurs = service.opprett(nyttKurs, accessToken);

                ctx.status(201).json(lagretKurs);

            } catch (ValideringException e) {
                ctx.status(400).result(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Kunne ikke lagre kurs");
            }
        });
    }
}
