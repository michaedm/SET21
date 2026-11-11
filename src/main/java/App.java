import controller.ChatController;
import controller.KursController;
import controller.LokallagController;
import controller.NyhetController;
import io.javalin.Javalin;
import config.SupabaseClient;
import repository.KursRepository;
import repository.NyhetRepository;
import repository.LokallagRepository;

public class App {
    public static void main(String[] args) {
        // Render oppgir porten i PORT; lokalt brukes 7070
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(port);

        SupabaseClient supabaseClient = new SupabaseClient();
        KursRepository kursRepository = new KursRepository(supabaseClient);
        NyhetRepository nyhetRepository = new NyhetRepository(supabaseClient);
        NyhetController nyhetController = new NyhetController(nyhetRepository);
        nyhetController.registerRoutes(app);
        LokallagRepository lokallagRepository = new LokallagRepository(supabaseClient);

        LokallagController.registerRoutes(app, lokallagRepository);
        KursController.registerRoutes(app, kursRepository);
        ChatController.registerRoutes(app);
    }
}