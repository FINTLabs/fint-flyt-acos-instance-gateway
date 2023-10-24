package no.fintlabs.model.acos;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class AcosInstanceMetadata {

    @NotBlank
    private String formId;

    @NotBlank
    private String instanceId;

    private String instanceUri;
}
