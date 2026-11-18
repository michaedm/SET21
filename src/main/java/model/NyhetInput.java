package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Det en redaktør sender inn når en nyhet opprettes eller endres (POST/PUT /api/nyheter).
 * nyhet_id er ikke med: den bestemmer databasen.
 * Ukjente felt (f.eks. nyhet_id fra frontend) ignoreres.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NyhetInput(
    @JsonProperty("tittel") String tittel,
    @JsonProperty("innhold") String innhold,
    @JsonProperty("bilde_url") String bildeUrl,
    @JsonProperty("publisert_dato") String publisertDato,
    @JsonProperty("lokallag_id") Integer lokallagId
) {

    public static final int MAKS_TITTEL = 200;
    public static final int MAKS_INNHOLD = 50_000;

    /** Returnerer en liste med feilmeldinger. Tom liste betyr at nyheten er gyldig. */
    public List<String> valider() {
        List<String> feil = new ArrayList<>();

        if (tittel == null || tittel.isBlank()) {
            feil.add("tittel må fylles ut");
        } else if (tittel.trim().length() > MAKS_TITTEL) {
            feil.add("tittel kan ikke være lengre enn " + MAKS_TITTEL + " tegn");
        }

        if (innhold != null && innhold.length() > MAKS_INNHOLD) {
            feil.add("innhold kan ikke være lengre enn " + MAKS_INNHOLD + " tegn");
        }

        if (lokallagId == null || lokallagId <= 0) {
            feil.add("lokallag_id må være et positivt tall");
        }

        if (publisertDato != null && !publisertDato.isBlank()) {
            try {
                LocalDate.parse(publisertDato.trim());
            } catch (DateTimeParseException e) {
                feil.add("publisert_dato må ha formatet ÅÅÅÅ-MM-DD");
            }
        }

        if (bildeUrl != null && !bildeUrl.isBlank()) {
            String url = bildeUrl.trim().toLowerCase();
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                feil.add("bilde_url må starte med http:// eller https://");
            }
        }

        return feil;
    }

    /**
     * Rydder opp før lagring: fjerner mellomrom rundt tekst, gjør tomme felt om til null,
     * og bruker dagens dato hvis publisert_dato mangler.
     */
    public NyhetInput klargjort(LocalDate idag) {
        String dato = (publisertDato == null || publisertDato.isBlank())
                ? idag.toString()
                : publisertDato.trim();

        return new NyhetInput(
                tittel == null ? null : tittel.trim(),
                tomTilNull(innhold),
                tomTilNull(bildeUrl),
                dato,
                lokallagId
        );
    }

    private static String tomTilNull(String tekst) {
        if (tekst == null || tekst.isBlank()) {
            return null;
        }
        return tekst.trim();
    }
}
