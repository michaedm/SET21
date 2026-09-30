import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7070);
    }
}