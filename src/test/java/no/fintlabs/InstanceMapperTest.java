package no.fintlabs;

import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.model.acos.AcosDocument;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.acos.AcosInstanceElement;
import no.fintlabs.model.acos.AcosInstanceMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class InstanceMapperTest {

    private FileClient fileClient;
    private AcosInstanceMapper acosInstanceMapper;

    @BeforeEach
    void setup() {
        fileClient = mock(FileClient.class);
        acosInstanceMapper = new AcosInstanceMapper(fileClient);
    }

    @Test
    void shouldMapToInstance() {
        AcosInstance acosInstance = AcosInstance
                .builder()
                .metadata(
                        AcosInstanceMetadata
                                .builder()
                                .formId("TEST0488")
                                .instanceId("100384")
                                .build()
                )
                .formPdfBase64("formPdfBase64Value")
                .elements(List.of(
                        AcosInstanceElement.builder().id("Fornavn").value("Ola").build(),
                        AcosInstanceElement.builder().id("Etternavn").value("Nordmann").build(),
                        AcosInstanceElement.builder().id("Fornavn2").value("Kari").build(),
                        AcosInstanceElement.builder().id("Etternavn2").value("Ødegård").build(),
                        AcosInstanceElement.builder().id("Ukedag").value(null).build(),
                        AcosInstanceElement.builder().id("Farge_pa_bil").value("Grønn").build()
                ))
                .documents(List.of(
                        AcosDocument.builder()
                                .name("vedleggImageNavn")
                                .type(MediaType.IMAGE_JPEG)
                                .encoding(null)
                                .base64("vedleggImageBase64Value")
                                .build(),
                        AcosDocument.builder()
                                .name("vedleggVideoNavn")
                                .type(MediaType.parseMediaType("video/mp4"))
                                .encoding(null)
                                .base64("vedleggVideoBase64Value")
                                .build()
                ))
                .build();

        File expectedSkjemaPdfFile = File
                .builder()
                .name("skjemaPdf")
                .sourceApplicationId(1L)
                .sourceApplicationInstanceId("100384")
                .type(MediaType.APPLICATION_PDF)
                .encoding("UTF-8")
                .base64Contents("formPdfBase64Value")
                .build();

        File expectedVedleggImageFile = File
                .builder()
                .name("vedleggImageNavn")
                .sourceApplicationId(1L)
                .sourceApplicationInstanceId("100384")
                .type(MediaType.IMAGE_JPEG)
                .encoding(null)
                .base64Contents("vedleggImageBase64Value")
                .build();

        File expectedVedleggVideoFile = File
                .builder()
                .name("vedleggVideoNavn")
                .sourceApplicationId(1L)
                .sourceApplicationInstanceId("100384")
                .type(MediaType.parseMediaType("video/mp4"))
                .encoding(null)
                .base64Contents("vedleggVideoBase64Value")
                .build();

        when(fileClient.postFile(expectedSkjemaPdfFile)).thenReturn(Mono.just(UUID.fromString("391e9177-2790-469a-9f42-c8042731bc55")));
        when(fileClient.postFile(expectedVedleggImageFile)).thenReturn(Mono.just(UUID.fromString("dab3ecc8-2901-46f0-9553-2fbc3e71ae9e")));
        when(fileClient.postFile(expectedVedleggVideoFile)).thenReturn(Mono.just(UUID.fromString("5a15e2dd-29a7-41ac-a635-f4ab41d10d18")));

        InstanceObject instance = acosInstanceMapper.map(1L, acosInstance).block();

        InstanceObject expectedInstanceObject = InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "skjema.Fornavn", "Ola",
                        "skjema.Etternavn", "Nordmann",
                        "skjema.Fornavn2", "Kari",
                        "skjema.Etternavn2", "Ødegård",
                        "skjema.Ukedag", "",
                        "skjema.Farge_pa_bil", "Grønn",
                        "skjemaPdf", "391e9177-2790-469a-9f42-c8042731bc55"
                ))
                .objectCollectionPerKey(Map.of(
                        "vedlegg", List.of(
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "navn", "vedleggImageNavn",
                                                "type", "image/jpeg",
                                                "enkoding", "",
                                                "fil", "dab3ecc8-2901-46f0-9553-2fbc3e71ae9e"
                                        ))
                                        .build(),
                                InstanceObject
                                        .builder()
                                        .valuePerKey(Map.of(
                                                "navn", "vedleggVideoNavn",
                                                "type", "video/mp4",
                                                "enkoding", "",
                                                "fil", "5a15e2dd-29a7-41ac-a635-f4ab41d10d18"
                                        ))
                                        .build()
                        )
                ))
                .build();

        assertEquals(expectedInstanceObject, instance);
    }


}
