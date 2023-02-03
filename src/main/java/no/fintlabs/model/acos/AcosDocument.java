package no.fintlabs.model.acos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcosDocument {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private String encoding;

    @NotBlank
    private String base64;

}
