package no.fintlabs.model.acos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fintlabs.gateway.instance.validation.constraints.ValidBase64;
import org.springframework.http.MediaType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcosDocument {

    @NotBlank
    private String name;

    @NotNull
    private MediaType type;

    private String encoding;

    @NotBlank
    @ValidBase64
    private String base64;

}
