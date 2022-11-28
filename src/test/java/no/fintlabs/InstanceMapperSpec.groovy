package no.fintlabs

import no.fintlabs.gateway.instance.model.File
import no.fintlabs.gateway.instance.model.instance.Document
import no.fintlabs.gateway.instance.model.instance.Instance
import no.fintlabs.gateway.instance.model.instance.InstanceField
import no.fintlabs.gateway.instance.web.FileClient
import no.fintlabs.model.acos.AcosDocument
import no.fintlabs.model.acos.AcosInstance
import no.fintlabs.model.acos.AcosInstanceElement
import no.fintlabs.model.acos.AcosInstanceMetadata
import reactor.core.publisher.Mono
import spock.lang.Specification

class InstanceMapperSpec extends Specification {

    private FileClient fileClient
    private AcosInstanceMapper acosInstanceMapper
    private AcosInstance acosInstance
    private Instance expectedInstance

    def setup() {
        fileClient = Mock(FileClient.class)
        acosInstanceMapper = new AcosInstanceMapper(fileClient)

        acosInstance = AcosInstance
                .builder()
                .metadata(
                        AcosInstanceMetadata
                                .builder()
                                .formId("TEST0488")
                                .instanceId("100384")
                                .instanceUri("https://acos.com/form-instance?id=100384")
                                .build()
                )
                .elements(List.of(
                        AcosInstanceElement.builder().id("Fornavn").value("Ola").build(),
                        AcosInstanceElement.builder().id("Etternavn").value("Nordmann").build(),
                        AcosInstanceElement.builder().id("Fornavn2").value("Kari").build(),
                        AcosInstanceElement.builder().id("Etternavn2").value("Ødegård").build(),
                        AcosInstanceElement.builder().id("Ukedag").value(null).build(),
                        AcosInstanceElement.builder().id("Farge_pa_bil").value("Grønn").build(),

                ))
                .documents(List.of(
                        AcosDocument.builder()
                                .name("dokumentnavn")
                                .type("pdfa")
                                .encoding("UTF-8")
                                .base64("base64String")
                                .build()
                ))
                .build()

        expectedInstance = Instance
                .builder()
                .sourceApplicationInstanceUri("https://acos.com/form-instance?id=100384")
                .fieldPerKey(Map.of(
                        "Fornavn", InstanceField.builder().key("Fornavn").value("Ola").build(),
                        "Etternavn", InstanceField.builder().key("Etternavn").value("Nordmann").build(),
                        "Fornavn2", InstanceField.builder().key("Fornavn2").value("Kari").build(),
                        "Etternavn2", InstanceField.builder().key("Etternavn2").value("Ødegård").build(),
                        "Ukedag", InstanceField.builder().key("Ukedag").value(null).build(),
                        "Farge_pa_bil", InstanceField.builder().key("Farge_pa_bil").value("Grønn").build(),
                ))
                .documents(List.of(
                        Document
                                .builder()
                                .name("dokumentnavn")
                                .type("pdfa")
                                .encoding("UTF-8")
                                .fileId(UUID.fromString("dab3ecc8-2901-46f0-9553-2fbc3e71ae9e"))
                                .build()
                ))
                .build()
    }

    def 'should map to instance'() {
        given:
        File expectedFile = File
                .builder()
                .name("dokumentnavn")
                .sourceApplicationId(1)
                .sourceApplicationInstanceId("100384")
                .type("pdfa")
                .encoding("UTF-8")
                .base64Contents("base64String")
                .build()

        when:
        Instance instance = acosInstanceMapper.map(1, acosInstance).block()

        then:
        1 * fileClient.postFile(expectedFile) >> Mono.just(UUID.fromString("dab3ecc8-2901-46f0-9553-2fbc3e71ae9e"))
        instance == expectedInstance
    }

}
