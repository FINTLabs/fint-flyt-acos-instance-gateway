package no.fintlabs;

import no.fintlabs.model.SourceApplicationIdAndSourceApplicationIntegrationId;
import no.fintlabs.model.acos.AcosDocument;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.acos.AcosInstanceElement;
import no.fintlabs.model.fint.File;
import no.fintlabs.model.fint.instance.Document;
import no.fintlabs.model.fint.instance.Instance;
import no.fintlabs.model.fint.instance.InstanceField;
import no.fintlabs.web.FileClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AcosInstanceMapper {

    private final FileClient fileClient;

    public AcosInstanceMapper(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    public Mono<Instance> toInstance(
            AcosInstance acosInstance,
            SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId
    ) {
        return toDocuments(acosInstance.getDocuments(), sourceApplicationIdAndSourceApplicationIntegrationId)
                .map(documents -> Instance
                        .builder()
                        .sourceApplicationInstanceUri(acosInstance.getMetadata().getInstanceUri())
                        .fieldPerKey(toFieldPerKey(acosInstance.getElements()))
                        .documents(documents)
                        .build());
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

    private Mono<List<Document>> toDocuments(
            List<AcosDocument> acosDocuments,
            SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId
    ) {
        return Flux.fromIterable(acosDocuments)
                .flatMap(acosDocument -> toDocument(acosDocument, sourceApplicationIdAndSourceApplicationIntegrationId))
                .collectList();
    }

    private Mono<Document> toDocument(
            AcosDocument acosDocument,
            SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId
    ) {
        File file = toFile(acosDocument, sourceApplicationIdAndSourceApplicationIntegrationId);
        return fileClient.postFile(file)
                .map(fileId -> toDocument(acosDocument, fileId));
    }

    private File toFile(
            AcosDocument acosDocument,
            SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId
    ) {
        return File
                .builder()
                .name(acosDocument.getName())
                .sourceApplicationId(sourceApplicationIdAndSourceApplicationIntegrationId.getSourceApplicationId())
                .sourceApplicationInstanceId(sourceApplicationIdAndSourceApplicationIntegrationId.getSourceApplicationIntegrationId())
                .type(acosDocument.getType())
                .encoding(acosDocument.getEncoding())
                .base64Contents(acosDocument.getBase64())
                .build();
    }

    private Document toDocument(AcosDocument acosDocument, UUID fileId) {
        return Document
                .builder()
                .name(acosDocument.getName())
                .type(acosDocument.getType())
                .encoding(acosDocument.getEncoding())
                .fileId(fileId)
                .build();
    }

}
