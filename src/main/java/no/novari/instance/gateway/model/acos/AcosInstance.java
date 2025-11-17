package no.novari.instance.gateway.model.acos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.novari.flyt.instance.gateway.validation.constraints.ValidBase64;
import no.novari.instance.gateway.validation.UniqueElementIds;

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
