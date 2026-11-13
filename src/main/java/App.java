import config.SupabaseClient;
import controller.ChatController;
import controller.KursController;
import controller.LokallagController;
import controller.NyhetController;
import io.javalin.Javalin;
import repository.KursRepository;
import repository.LokallagRepository;
import repository.NyhetRepository;
import service.KursService;
import service.LokallagService;
import service.NyhetService;
import controller.PaameldingController;
import repository.PaameldingRepository;
import service.PaameldingService;

public class App {
    public static void main(String[] args) {
        // Render oppgir porten i PORT; lokalt brukes 7070
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(port);

        // Dependency injection: her lages hvert objekt én gang og sendes videre til den som trenger det.
        // Rekkefølge: SupabaseClient -> Repository -> Service -> Controller
        SupabaseClient supabaseClient = new SupabaseClient();

        NyhetRepository nyhetRepository = new NyhetRepository(supabaseClient);
        KursRepository kursRepository = new KursRepository(supabaseClient);
        LokallagRepository lokallagRepository = new LokallagRepository(supabaseClient);

        NyhetService nyhetService = new NyhetService(nyhetRepository);
        KursService kursService = new KursService(kursRepository);
        LokallagService lokallagService = new LokallagService(lokallagRepository);

        PaameldingRepository paameldingRepository = new PaameldingRepository(supabaseClient);
        PaameldingService paameldingService = new PaameldingService(paameldingRepository);
        PaameldingController paameldingController = new PaameldingController(paameldingService);

        new NyhetController(nyhetService).registerRoutes(app);
        new KursController(kursService).registerRoutes(app);
        new LokallagController(lokallagService).registerRoutes(app);
        ChatController.registerRoutes(app);
        paameldingController.registerRoutes(app);
    }
}
