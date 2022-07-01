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
    private String type;

    // TODO: 01/07/2022 Add validation when ACOS has decided on document format
    // TODO: 01/07/2022 Remove uri or base64  when ACOS has decided on document format
    private String uri;

    private String base64;

}
