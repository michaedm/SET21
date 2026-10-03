import controller.KursController;
import controller.LokallagController;
import controller.NyhetController;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7070);
        NyhetController.registerRoutes(app);
        LokallagController.registerRoutes(app);
        KursController.registerRoutes(app);
    }
}