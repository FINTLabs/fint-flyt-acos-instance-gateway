package no.fintlabs.model.caseinfo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@EqualsAndHashCode
@Jacksonized
public class AdministrativeUnit {
    private final String name;
}
