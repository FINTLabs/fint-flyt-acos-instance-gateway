package no.fintlabs.model.acos;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.gateway.instance.validation.constraints.ValidBase64;
import org.springframework.http.MediaType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class AcosDocument {

    @NotBlank
    private String name;

    @NotNull
    private MediaType type;

    private String encoding;

    @NotNull
    @ValidBase64
    private String base64;
}
