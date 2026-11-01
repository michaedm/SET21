package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Kurs(
    @JsonProperty("kurs_id") int kursId,
    @JsonProperty("tittel") String tittel,
    @JsonProperty("tema") String tema,
    @JsonProperty("beskrivelse") String beskrivelse,
    @JsonProperty("sted") String sted,
    @JsonProperty("startdato") String startdato,
    @JsonProperty("sluttdato") String sluttdato,
    @JsonProperty("maks_deltakere") int maksDeltakere,
    @JsonProperty("pris") double pris,
    @JsonProperty("status") String status,
    @JsonProperty("lokallag_id") int lokallagId
) {}