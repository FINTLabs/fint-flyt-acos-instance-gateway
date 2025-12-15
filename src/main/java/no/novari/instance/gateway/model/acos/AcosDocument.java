package no.novari.instance.gateway.model.acos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.novari.flyt.instance.gateway.validation.constraints.ValidBase64;
import org.springframework.http.MediaType;

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
