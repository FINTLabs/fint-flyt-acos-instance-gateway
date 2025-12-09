package no.novari.instance.gateway.model.acos;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

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
