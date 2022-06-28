package no.fintlabs.model.acos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcosInstance {
    private AcosInstanceMetadata metadata;
    private List<AcosInstanceElement> elements;
    private List<AcosDocument> documents;
}
