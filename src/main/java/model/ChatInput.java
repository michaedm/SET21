package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Det nettleseren sender til chatboten (POST /api/chat).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatInput(
    @JsonProperty("melding") String melding
) {

    public static final int MAKS_MELDING = 500;

    /** Returnerer en liste med feilmeldinger. Tom liste betyr at meldingen er gyldig. */
    public List<String> valider() {
        List<String> feil = new ArrayList<>();

        if (melding == null || melding.isBlank()) {
            feil.add("melding må fylles ut");
        } else if (melding.trim().length() > MAKS_MELDING) {
            feil.add("melding kan ikke være lengre enn " + MAKS_MELDING + " tegn");
        }

        return feil;
    }
}
