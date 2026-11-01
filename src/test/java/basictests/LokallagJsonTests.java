package basictests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Lokallag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Enhetstester for Lokallag.
 */
public class LokallagJsonTests {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("JSON fra databasen blir til et Lokallag")
    public void jsonFromDatabaseBecomesLokallag() throws Exception {
        Lokallag lokallag = mapper.readValue(
                "{\"lokallag_id\": 3, \"navn\": \"Halden Husflidslag\"}", Lokallag.class);

        Assertions.assertEquals(3, lokallag.lokallagId());
        Assertions.assertEquals("Halden Husflidslag", lokallag.navn());
    }

    @Test
    @DisplayName("Norske bokstaver (æ, ø, å) beholdes")
    public void norwegianLettersAreKept() throws Exception {
        Lokallag lokallag = mapper.readValue(
                "{\"lokallag_id\": 1, \"navn\": \"Østfold Husflidslag – Råde og Ærø\"}", Lokallag.class);

        Assertions.assertEquals("Østfold Husflidslag – Råde og Ærø", lokallag.navn());
    }

    @Test
    @DisplayName("Liste med ett lokallag gir nøyaktig ett element")
    public void singleElementList() throws Exception {
        List<Lokallag> liste = mapper.readValue(
                "[{\"lokallag_id\": 1, \"navn\": \"Moss\"}]", new TypeReference<List<Lokallag>>() {});

        Assertions.assertEquals(1, liste.size());
        Assertions.assertEquals("Moss", liste.get(0).navn());
    }

    @Test
    @DisplayName("Lokallag blir til JSON med feltnavnene lokallag_id og navn")
    public void lokallagBecomesJson() {
        JsonNode json = mapper.valueToTree(new Lokallag(4, "Sarpsborg"));

        Assertions.assertEquals(4, json.get("lokallag_id").asInt());
        Assertions.assertEquals("Sarpsborg", json.get("navn").asText());
    }
}
