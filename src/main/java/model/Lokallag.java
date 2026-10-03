package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Lokallag(
    @JsonProperty("lokallag_id") int lokallagId,
    @JsonProperty("navn") String navn
) {}