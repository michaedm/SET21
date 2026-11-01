package basictests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Kurs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Enhetstester for Kurs: tall, datoer og felt som kan mangle (sluttdato, tema osv.).
 */
public class KursJsonTests {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("JSON fra databasen blir til et Kurs med riktige felt")
    public void jsonFromDatabaseBecomesKurs() throws Exception {
        String json = """
                {"kurs_id": 5, "tittel": "Strikking for nybegynnere", "tema": "Strikk",
                 "beskrivelse": "Lær grunnleggende strikking", "sted": "Fredrikstad",
                 "startdato": "2026-11-01", "sluttdato": "2026-11-15",
                 "maks_deltakere": 12, "pris": 450.50, "status": "aktiv", "lokallag_id": 2}
                """;

        Kurs kurs = mapper.readValue(json, Kurs.class);

        Assertions.assertEquals(5, kurs.kursId());
        Assertions.assertEquals("Strikking for nybegynnere", kurs.tittel());
        Assertions.assertEquals("2026-11-01", kurs.startdato());
        Assertions.assertEquals("2026-11-15", kurs.sluttdato());
        Assertions.assertEquals(12, kurs.maksDeltakere());
        Assertions.assertEquals(450.50, kurs.pris(), 0.001);
        Assertions.assertEquals("aktiv", kurs.status());
        Assertions.assertEquals(2, kurs.lokallagId());
    }

    @Test
    @DisplayName("Kurs uten sluttdato, tema, beskrivelse og sted (null) går fint")
    public void optionalFieldsCanBeNull() throws Exception {
        String json = """
                {"kurs_id": 6, "tittel": "Endagskurs", "tema": null, "beskrivelse": null,
                 "sted": null, "startdato": "2026-12-01", "sluttdato": null,
                 "maks_deltakere": 8, "pris": 0, "status": "aktiv", "lokallag_id": 1}
                """;

        Kurs kurs = mapper.readValue(json, Kurs.class);

        Assertions.assertNull(kurs.sluttdato());
        Assertions.assertNull(kurs.tema());
        Assertions.assertNull(kurs.sted());
    }

    @Test
    @DisplayName("Gratis kurs (pris 0) og heltallspris leses riktig")
    public void zeroAndWholeNumberPrice() throws Exception {
        String gratis = """
                {"kurs_id": 7, "tittel": "Gratis", "startdato": "2026-12-01",
                 "maks_deltakere": 10, "pris": 0, "status": "aktiv", "lokallag_id": 1}
                """;
        String heltall = """
                {"kurs_id": 8, "tittel": "Heltall", "startdato": "2026-12-01",
                 "maks_deltakere": 10, "pris": 300, "status": "aktiv", "lokallag_id": 1}
                """;

        Assertions.assertEquals(0.0, mapper.readValue(gratis, Kurs.class).pris(), 0.001);
        Assertions.assertEquals(300.0, mapper.readValue(heltall, Kurs.class).pris(), 0.001);
    }

    @Test
    @DisplayName("Kurs blir til JSON med snake_case-feltnavn")
    public void kursBecomesSnakeCaseJson() {
        Kurs kurs = new Kurs(1, "Veving", "Vev", "Beskrivelse", "Moss",
                "2026-11-01", null, 10, 200.0, "aktiv", 3);

        JsonNode json = mapper.valueToTree(kurs);

        Assertions.assertEquals(10, json.get("maks_deltakere").asInt());
        Assertions.assertEquals(3, json.get("lokallag_id").asInt());
        Assertions.assertTrue(json.get("sluttdato").isNull());
        Assertions.assertFalse(json.has("maksDeltakere"));
    }
}
