package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceElement;
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
    public Mono<InstanceElement> map(
            Long sourceApplicationId,
            AcosInstance acosInstance
    ) {
        return Mono.zip(
                        mapPdfFileToFileId(sourceApplicationId, acosInstance),
                        mapDocumentsToInstanceElements(sourceApplicationId, acosInstance.getMetadata().getInstanceId(), acosInstance.getDocuments())
                )
                .map((Tuple2<UUID, List<InstanceElement>> formPdfFileIdAndDocumentInstanceElement) -> InstanceElement
                        .builder()
                        .valuePerKey(toValuePerKey(
                                acosInstance.getElements(),
                                formPdfFileIdAndDocumentInstanceElement.getT1()
                        ))
                        .elementCollectionPerKey(Map.of(
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
                        .type(MediaType.APPLICATION_PDF_VALUE)
                        .sourceApplicationId(sourceApplicationId)
                        .sourceApplicationInstanceId(acosInstance.getMetadata().getInstanceId())
                        .encoding("UTF-8")
                        .base64Contents(acosInstance.getFormPdfBase64())
                        .build()
        );
    }

    private Mono<List<InstanceElement>> mapDocumentsToInstanceElements(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            Collection<AcosDocument> acosDocuments
    ) {
        return Flux.fromIterable(acosDocuments)
                .flatMap(acosDocument -> mapDocumentToInstanceElement(sourceApplicationId, sourceApplicationInstanceId, acosDocument))
                .collectList();
    }

    private Mono<InstanceElement> mapDocumentToInstanceElement(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            AcosDocument acosDocument
    ) {
        File file = toFile(sourceApplicationId, sourceApplicationInstanceId, acosDocument);
        return fileClient.postFile(file)
                .map(fileId -> toInstanceElement(acosDocument, fileId));
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

    private InstanceElement toInstanceElement(AcosDocument acosDocument, UUID fileId) {
        return InstanceElement
                .builder()
                .valuePerKey(Map.of(
                        "navn", acosDocument.getName(),
                        "type", acosDocument.getType(),
                        "enkoding", Optional.ofNullable(acosDocument.getEncoding()).orElse(""),
                        "fil", fileId.toString()
                ))
                .build();
    }

}
