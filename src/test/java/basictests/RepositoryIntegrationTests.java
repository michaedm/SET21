package basictests;

import model.Kurs;
import model.Lokallag;
import model.Nyhet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.KursRepository;
import repository.LokallagRepository;
import repository.NyhetRepository;

import java.util.List;

/**
 * Integrasjonstester: ekte repository-klasser + falsk Supabase (FakeSupabase).
 * Sjekker at riktig tabell spørres, med riktig sortering og API-nøkkel,
 * og at svaret blir til riktige Java-objekter.
 */
public class RepositoryIntegrationTests {

    private static FakeSupabase supabase;

    @BeforeAll
    public static void startFakeSupabase() throws Exception {
        supabase = new FakeSupabase();
        supabase.start();
    }

    @AfterAll
    public static void stopFakeSupabase() {
        supabase.stop();
    }

    @BeforeEach
    public void resetFakeSupabase() {
        supabase.reset();
    }

    @Test
    @DisplayName("NyhetRepository henter fra tabellen 'publish' og sender API-nøkkel")
    public void nyhetRepositoryAsksForPublishTable() throws Exception {
        supabase.respondWith(200, """
                [{"nyhet_id": 1, "tittel": "Test", "innhold": "Dette er en test",
                  "bilde_url": "", "publisert_dato": "2026-09-30", "lokallag_id": 1}]
                """);

        List<Nyhet> nyheter = new NyhetRepository().hentAlle();

        Assertions.assertEquals("/rest/v1/publish", supabase.lastPath());
        Assertions.assertEquals("test-key", supabase.lastApiKey());
        Assertions.assertEquals(1, nyheter.size());
        Assertions.assertEquals("Test", nyheter.get(0).tittel());
    }

    @Test
    @DisplayName("LokallagRepository henter fra 'lokallag' sortert på navn")
    public void lokallagRepositorySortsByName() throws Exception {
        supabase.respondWith(200, """
                [{"lokallag_id": 2, "navn": "Halden"}, {"lokallag_id": 1, "navn": "Moss"}]
                """);

        List<Lokallag> lokallag = new LokallagRepository().hentAlle();

        Assertions.assertEquals("/rest/v1/lokallag", supabase.lastPath());
        Assertions.assertTrue(supabase.lastQuery().contains("order=navn.asc"));
        Assertions.assertEquals(2, lokallag.size());
        Assertions.assertEquals("Halden", lokallag.get(0).navn());
    }

    @Test
    @DisplayName("KursRepository henter fra 'kurs' sortert på startdato")
    public void kursRepositorySortsByStartDate() throws Exception {
        supabase.respondWith(200, """
                [{"kurs_id": 1, "tittel": "Strikk", "tema": null, "beskrivelse": null,
                  "sted": "Moss", "startdato": "2026-11-01", "sluttdato": null,
                  "maks_deltakere": 10, "pris": 250, "status": "aktiv", "lokallag_id": 1}]
                """);

        List<Kurs> kurs = new KursRepository().hentAlle();

        Assertions.assertEquals("/rest/v1/kurs", supabase.lastPath());
        Assertions.assertTrue(supabase.lastQuery().contains("order=startdato.asc"));
        Assertions.assertEquals(1, kurs.size());
        Assertions.assertEquals("Strikk", kurs.get(0).tittel());
    }

    @Test
    @DisplayName("Tom tabell gir tom liste")
    public void emptyTableGivesEmptyList() throws Exception {
        supabase.respondWith(200, "[]");

        Assertions.assertTrue(new NyhetRepository().hentAlle().isEmpty());
        Assertions.assertTrue(new LokallagRepository().hentAlle().isEmpty());
        Assertions.assertTrue(new KursRepository().hentAlle().isEmpty());
    }

    @Test
    @DisplayName("Feilmelding fra Supabase gir exception, ikke en feil liste")
    public void supabaseErrorThrows() {
        supabase.respondWith(401, """
                {"message": "Invalid API key"}
                """);

        Assertions.assertThrows(Exception.class, () -> new NyhetRepository().hentAlle());
    }
}
