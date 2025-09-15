package no.fintlabs.model.acos;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.gateway.instance.validation.constraints.ValidBase64;
import no.fintlabs.validation.UniqueElementIds;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class AcosInstance {

    @NotNull
    @Valid
    private AcosInstanceMetadata metadata;

    @NotEmpty
    @UniqueElementIds
    @Valid
    private List<@NotNull AcosInstanceElement> elements;

    @NotBlank
    @ValidBase64
    private String formPdfBase64;

    @Builder.Default
    @Valid
    private List<@NotNull AcosDocument> documents = new ArrayList<>();
}
