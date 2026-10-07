package controller;
import io.javalin.http.Context;
import model.Nyhet;
import repository.NyhetRepository;
import java.util.List;

public class NyhetController {

    private NyhetRepository nyhetRepository;
    public NyhetController(NyhetRepository nyhetRepository){
        this.nyhetRepository = nyhetRepository;
    }
    public void getAllNyheter(Context ctx) throws Exception{
        List<Nyhet> nyheter = nyhetRepository.hentAlle();
        ctx.json(nyheter);
    }
}