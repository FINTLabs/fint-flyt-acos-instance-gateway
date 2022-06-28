package no.fintlabs;

import no.fintlabs.model.acos.AcosDocument;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.acos.AcosInstanceElement;
import no.fintlabs.model.fint.Document;
import no.fintlabs.model.fint.Instance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AcosInstanceMapper {

    public Instance toInstance(AcosInstance acosInstance) {
        return Instance
                .builder()
                .sourceApplicationInstanceUri(acosInstance.getMetadata().getInstanceUri())
                .fieldValuePerId(toFieldValuePerId(acosInstance.getElements()))
                .documents(toDocuments(acosInstance.getDocuments()))
                .build();
    }

    private Map<String, String> toFieldValuePerId(List<AcosInstanceElement> acosInstanceElements) {
        return acosInstanceElements
                .stream()
                .collect(Collectors.toMap(
                        AcosInstanceElement::getId,
                        AcosInstanceElement::getValue
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
