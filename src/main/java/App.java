import io.javalin.Javalin;
import controller.NyhetController;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7070);
        NyhetController.registerRoutes(app);
    }
}