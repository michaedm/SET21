# Arkitektur (backend)

Backend er delt i lag. Hvert lag har ett ansvar og kjenner bare laget under seg.

```
Nettleser  →  Controller  →  Service  →  Repository  →  SupabaseClient  →  Supabase
              (HTTP)         (regler)    (database)      (HTTP mot Supabase)
```

| Lag | Pakke | Ansvar | Eksempel |
|---|---|---|---|
| Controller | `controller` | Ruter, leser forespørselen (JSON, token, parametere), velger statuskode | `NyhetController` |
| Service | `service` | Forretningsregler: validering, standardverdier, «finnes den?» | `NyhetService` |
| Repository | `repository` | Spørringer mot tabellene i Supabase | `NyhetRepository` |
| Klient | `config` | Felles oppsett mot Supabase (URL, nøkkel, feilhåndtering) | `SupabaseClient` |
| Modell | `model` | Dataklasser (`Nyhet`, `Kurs`, `Lokallag`, `NyhetInput`) | |

## Dependency injection

Ingen klasse lager sine egne avhengigheter med `new`. De sendes inn i konstruktøren:

```java
public class NyhetService {
    private final NyhetRepository repository;

    public NyhetService(NyhetRepository repository) {
        this.repository = repository;
    }
}
```

`App.java` er det eneste stedet som setter sammen alt:

```java
SupabaseClient supabaseClient = new SupabaseClient();
NyhetRepository nyhetRepository = new NyhetRepository(supabaseClient);
NyhetService nyhetService = new NyhetService(nyhetRepository);
new NyhetController(nyhetService).registerRoutes(app);
```

Fordelen er at testene kan sende inn noe annet. Service-testene bruker for eksempel et falskt
repository (ingen database) og en fast klokke (`Clock.fixed`), så reglene kan testes alene.

## Feil

Servicene kaster `ValideringException` (ugyldige data) og `IkkeFunnetException` (finnes ikke).
Controllerne gjør dem om til 400 og 404. Feil fra databasen (`SupabaseException`) blir 401, 403, 400 eller 500.
