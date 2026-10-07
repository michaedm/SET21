package controller;

import java.util.List;

import io.javalin.Javalin;
import model.Kurs;
import repository.KursRepository;

public class KursController {

    private static final KursRepository repository = new KursRepository();

    public static void registerRoutes(Javalin app) {

        app.get("/api/kurs", ctx -> {
            List<Kurs> kurs = repository.hentAlle();
            ctx.json(kurs);
        });
    }
}