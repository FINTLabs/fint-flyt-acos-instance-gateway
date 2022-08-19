package no.fintlabs;

import no.fintlabs.model.acos.AcosDocument;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.acos.AcosInstanceElement;
import no.fintlabs.model.fint.Document;
import no.fintlabs.model.fint.Instance;
import no.fintlabs.model.fint.InstanceField;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AcosInstanceMapper {

    public Instance toInstance(AcosInstance acosInstance) {
        return Instance
                .builder()
                .sourceApplicationInstanceUri(acosInstance.getMetadata().getInstanceUri())
                .fieldPerKey(toFieldPerKey(acosInstance.getElements()))
                .documents(toDocuments(acosInstance.getDocuments()))
                .build();
    }

    private Map<String, InstanceField> toFieldPerKey(List<AcosInstanceElement> acosInstanceElements) {
        return acosInstanceElements
                .stream()
                .map(acosInstanceElement -> InstanceField
                        .builder()
                        .key(acosInstanceElement.getId())
                        .value(acosInstanceElement.getValue())
                        .build()
                )
                .collect(Collectors.toMap(
                        InstanceField::getKey,
                        Function.identity()
                ));
    }

    private List<Document> toDocuments(List<AcosDocument> acosDocuments) {
        return acosDocuments
                .stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
    }

    private Document toDocument(AcosDocument acosDocument) {
        return Document
                .builder()
                .type(acosDocument.getType())
                .uri(acosDocument.getUri())
                .base64(acosDocument.getBase64())
                .build();
    }

}
