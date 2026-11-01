package controller;

import io.javalin.Javalin;
import model.Nyhet;
import repository.NyhetRepository;
import java.util.List;

public class NyhetController {

    private static final NyhetRepository repository = new NyhetRepository();

    public static void registerRoutes(Javalin app) {

        app.get("/api/nyheter", ctx -> {

            // Hardkodet testdata som ble brukt før repository
            /*
            Nyhet nyhet = new Nyhet(
                1,
                "Test",
                "Dette er en test",
                "",
                "2026-09-30",
                1
            );

            List<Nyhet> nyheter = List.of(nyhet);
            */
            List<Nyhet> nyheter = repository.hentAlle();
            ctx.json(nyheter);
        });
    }
}