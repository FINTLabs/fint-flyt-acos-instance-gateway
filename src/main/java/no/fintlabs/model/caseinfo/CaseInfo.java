package no.fintlabs.model.caseinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@EqualsAndHashCode
@Jacksonized
public class CaseInfo {
    @JsonProperty("instanceId")
    private final String sourceApplicationInstanceId;
    private final String archiveCaseId;
    private final CaseManager caseManager;
    private final AdministrativeUnit administrativeUnit;
    private final CaseStatus status;
}
