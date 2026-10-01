package controller;

import io.javalin.Javalin;
import model.Nyhet;
import java.util.List;

public class NyhetController {

    public static void registerRoutes(Javalin app) {

        app.get("/api/nyheter", ctx -> {

            Nyhet nyhet = new Nyhet(
                1,
                "Test",
                "Dette er en test",
                "",
                "2026-09-30",
                1
            );

            List<Nyhet> nyheter = List.of(nyhet);

            ctx.json(nyheter);
        });
    }
}