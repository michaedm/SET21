package controller;

import io.javalin.Javalin;
import service.LokallagService;

public class LokallagController {

    private final LokallagService service;

    public LokallagController(LokallagService service) {
        this.service = service;
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/lokallag", ctx -> ctx.json(service.hentAlle()));
    }
}
