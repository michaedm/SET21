package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Paamelding(
    @JsonProperty("kurs_id") int kursId,
    @JsonProperty("navn") String navn,
    @JsonProperty("epost") String epost,
    @JsonProperty("telefon") String telefon,
    @JsonProperty("status") String status
) {}