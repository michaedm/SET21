package basictests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Nyhet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Enhetstester for Nyhet: sjekker at JSON fra Supabase (snake_case) blir riktig Java-objekt,
 * og at objektet blir til den JSON-en som script.js forventer.
 */
public class NyhetJsonTests {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("JSON fra databasen blir til en Nyhet med riktige felt")
    public void jsonFromDatabaseBecomesNyhet() throws Exception {
        // Arrange
        String json = """
                {"nyhet_id": 1, "tittel": "Test", "innhold": "Dette er en test",
                 "bilde_url": "", "publisert_dato": "2026-09-30", "lokallag_id": 1}
                """;

        // Act
        Nyhet nyhet = mapper.readValue(json, Nyhet.class);

        // Assert
        Assertions.assertEquals(1, nyhet.nyhetId());
        Assertions.assertEquals("Test", nyhet.tittel());
        Assertions.assertEquals("Dette er en test", nyhet.innhold());
        Assertions.assertEquals("", nyhet.bildeUrl());
        Assertions.assertEquals("2026-09-30", nyhet.publisertDato());
        Assertions.assertEquals(1, nyhet.lokallagId());
    }

    @Test
    @DisplayName("Nyhet uten bilde og innhold (null) går fint")
    public void nullableFieldsCanBeNull() throws Exception {
        String json = """
                {"nyhet_id": 2, "tittel": "Uten bilde", "innhold": null,
                 "bilde_url": null, "publisert_dato": "2026-10-01", "lokallag_id": 1}
                """;

        Nyhet nyhet = mapper.readValue(json, Nyhet.class);

        Assertions.assertNull(nyhet.bildeUrl());
        Assertions.assertNull(nyhet.innhold());
        Assertions.assertEquals("Uten bilde", nyhet.tittel());
    }

    @Test
    @DisplayName("Nyhet blir til JSON med feltnavnene som script.js bruker")
    public void nyhetBecomesJsonWithFrontendFieldNames() {
        Nyhet nyhet = new Nyhet(1, "Test", "Innhold", "bilde.jpg", "2026-09-30", 1);

        JsonNode json = mapper.valueToTree(nyhet);

        Assertions.assertEquals("Test", json.get("tittel").asText());
        Assertions.assertEquals("Innhold", json.get("innhold").asText());
        Assertions.assertEquals("2026-09-30", json.get("publisert_dato").asText());
        Assertions.assertEquals("bilde.jpg", json.get("bilde_url").asText());
        // Java-navnet skal IKKE lekke ut til frontend
        Assertions.assertFalse(json.has("publisertDato"));
    }

    @Test
    @DisplayName("Tom liste fra databasen gir tom liste, ikke feil")
    public void emptyListGivesEmptyList() throws Exception {
        List<Nyhet> nyheter = mapper.readValue("[]", new TypeReference<List<Nyhet>>() {});

        Assertions.assertTrue(nyheter.isEmpty());
    }

    @Test
    @DisplayName("Liste med flere nyheter beholder antall og rekkefølge")
    public void listKeepsCountAndOrder() throws Exception {
        String json = """
                [{"nyhet_id": 1, "tittel": "Første", "innhold": "a", "bilde_url": "",
                  "publisert_dato": "2026-10-02", "lokallag_id": 1},
                 {"nyhet_id": 2, "tittel": "Andre", "innhold": "b", "bilde_url": "",
                  "publisert_dato": "2026-10-01", "lokallag_id": 2}]
                """;

        List<Nyhet> nyheter = mapper.readValue(json, new TypeReference<List<Nyhet>>() {});

        Assertions.assertEquals(2, nyheter.size());
        Assertions.assertEquals("Første", nyheter.get(0).tittel());
        Assertions.assertEquals("Andre", nyheter.get(1).tittel());
    }
}
