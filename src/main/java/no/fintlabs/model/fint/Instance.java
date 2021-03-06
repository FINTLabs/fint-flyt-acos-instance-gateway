package no.fintlabs.model.fint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Instance {
    private String sourceApplicationInstanceUri;
    private Map<String, InstanceField> fieldPerId;
    private List<Document> documents;
}
