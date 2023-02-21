package no.fintlabs.model.acos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fintlabs.gateway.instance.validation.constraints.ValidBase64;
import no.fintlabs.validation.UniqueElementIds;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @Valid
    private List<@NotNull AcosDocument> documents = new ArrayList<>();

}
