package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.model.acos.AcosDocument;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.acos.AcosInstanceElement;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AcosInstanceMapper implements InstanceMapper<AcosInstance> {

    private final FileClient fileClient;

    public AcosInstanceMapper(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    @Override
    public Mono<InstanceObject> map(
            Long sourceApplicationId,
            AcosInstance acosInstance
    ) {
        return Mono.zip(
                        mapPdfFileToFileId(sourceApplicationId, acosInstance),
                        mapDocumentsToInstanceObjects(sourceApplicationId, acosInstance.getMetadata().getInstanceId(), acosInstance.getDocuments())
                )
                .map((Tuple2<UUID, List<InstanceObject>> formPdfFileIdAndDocumentInstanceElement) -> InstanceObject
                        .builder()
                        .valuePerKey(toValuePerKey(
                                acosInstance.getElements(),
                                formPdfFileIdAndDocumentInstanceElement.getT1()
                        ))
                        .objectCollectionPerKey(Map.of(
                                "vedlegg", formPdfFileIdAndDocumentInstanceElement.getT2()
                        ))
                        .build());
    }

    private Map<String, String> toValuePerKey(
            Collection<AcosInstanceElement> acosInstanceElements,
            UUID formPdfFileid
    ) {
        Map<String, String> valuePerKey = acosInstanceElements
                .stream()
                .collect(Collectors.toMap(
                        acosInstanceElement -> "skjema." + acosInstanceElement.getId(),
                        acosInstanceElement -> Optional.ofNullable(acosInstanceElement.getValue()).orElse("")
                ));

        valuePerKey.put("skjemaPdf", formPdfFileid.toString());

        return valuePerKey;
    }

    private Mono<UUID> mapPdfFileToFileId(Long sourceApplicationId, AcosInstance acosInstance) {
        return fileClient.postFile(
                File
                        .builder()
                        .name("skjemaPdf")
                        .type(MediaType.APPLICATION_PDF)
                        .sourceApplicationId(sourceApplicationId)
                        .sourceApplicationInstanceId(acosInstance.getMetadata().getInstanceId())
                        .encoding("UTF-8")
                        .base64Contents(acosInstance.getFormPdfBase64())
                        .build()
        );
    }

    private Mono<List<InstanceObject>> mapDocumentsToInstanceObjects(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            Collection<AcosDocument> acosDocuments
    ) {
        return Flux.fromIterable(acosDocuments)
                .flatMap(acosDocument -> mapDocumentToInstanceObject(sourceApplicationId, sourceApplicationInstanceId, acosDocument))
                .collectList();
    }

    private Mono<InstanceObject> mapDocumentToInstanceObject(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            AcosDocument acosDocument
    ) {
        File file = toFile(sourceApplicationId, sourceApplicationInstanceId, acosDocument);
        return fileClient.postFile(file)
                .map(fileId -> toInstanceObject(acosDocument, fileId));
    }

    private File toFile(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            AcosDocument acosDocument
    ) {
        return File
                .builder()
                .name(acosDocument.getName())
                .sourceApplicationId(sourceApplicationId)
                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                .type(acosDocument.getType())
                .encoding(acosDocument.getEncoding())
                .base64Contents(acosDocument.getBase64())
                .build();
    }

    private InstanceObject toInstanceObject(AcosDocument acosDocument, UUID fileId) {
        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "navn", acosDocument.getName(),
                        "type", acosDocument.getType().toString(),
                        "enkoding", Optional.ofNullable(acosDocument.getEncoding()).orElse(""),
                        "fil", fileId.toString()
                ))
                .build();
    }

}
