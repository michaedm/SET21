package controller;

import java.util.List;

import io.javalin.Javalin;
import model.Lokallag;
import repository.LokallagRepository;

public class LokallagController {

    private static final LokallagRepository repository = new LokallagRepository();

    public static void registerRoutes(Javalin app) {

        app.get("/api/lokallag", ctx -> {
            List<Lokallag> lokallag = repository.hentAlle();
            ctx.json(lokallag);
        });
    }
}