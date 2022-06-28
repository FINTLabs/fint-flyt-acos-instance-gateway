package no.fintlabs.model.acos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcosDocument {
    private String type;
    private String uri; // TODO: 28/06/2022 Remove this or base64
    private String base64;
}
