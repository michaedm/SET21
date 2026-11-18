package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Nye kolonner i databasen skal ikke knekke API-et (funnet i basictests/README.md)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Nyhet(
    @JsonProperty("nyhet_id") int nyhetId,
    @JsonProperty("tittel") String tittel,
    @JsonProperty("innhold") String innhold,
    @JsonProperty("bilde_url") String bildeUrl,
    @JsonProperty("publisert_dato") String publisertDato,
    @JsonProperty("lokallag_id") int lokallagId
) {}