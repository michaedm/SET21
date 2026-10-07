import io.javalin.Javalin;
import repository.NyhetRepository;
import controller.NyhetController;

public class App {

    public static void main(String[] args) {
        NyhetRepository nyhetRepository = new NyhetRepository();
        NyhetController nyhetController = new NyhetController(nyhetRepository);
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7070);
        app.get("/api/nyheter", nyhetController::getAllNyheter);
    }
}