package no.fintlabs.model.caseinfo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@EqualsAndHashCode
@Jacksonized
public class CaseStatus {
    private final String name;
    private final String code;

    @Override
    public String toString() {
        return "Sensitive data omitted";
    }
}
