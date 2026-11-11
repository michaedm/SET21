import controller.ChatController;
import controller.KursController;
import controller.LokallagController;
import controller.NyhetController;
import io.javalin.Javalin;
import config.SupabaseClient;
import repository.KursRepository;

public class App {
    public static void main(String[] args) {
        // Render oppgir porten i PORT; lokalt brukes 7070
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(port);

        SupabaseClient supabaseClient = new SupabaseClient();
        KursRepository kursRepository = new KursRepository(supabaseClient);

        NyhetController.registerRoutes(app);
        LokallagController.registerRoutes(app);
        KursController.registerRoutes(app, kursRepository);
        ChatController.registerRoutes(app);
    }
}