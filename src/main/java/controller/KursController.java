
package controller;

import java.util.List;

import io.javalin.Javalin;
import model.Kurs;
import repository.KursRepository;

public class KursController {

    private static final KursRepository repository = new KursRepository();

    public static void registerRoutes(Javalin app) {

        app.get("/api/kurs", ctx -> {
            try {
                List<Kurs> kurs = repository.hentAlle();
                ctx.json(kurs);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Kunne ikke hente kurs");
            }
        });

        app.post("/api/kurs", ctx -> {
            try {
                // 1. Read the login token from the request
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

                // 2. Read the course data
                Kurs nyttKurs = ctx.bodyAsClass(Kurs.class);

                if (nyttKurs.tittel() == null || nyttKurs.tittel().isBlank()) {
                    ctx.status(400).result("Tittel er påkrevd");
                    return;
                }

                // 3. Save using the user's token
                Kurs lagretKurs = repository.opprett(nyttKurs, accessToken);

                ctx.status(201).json(lagretKurs);

            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Kunne ikke lagre kurs");
            }
        });
    }
}
