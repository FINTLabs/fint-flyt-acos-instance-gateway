package no.fintlabs;

import no.fintlabs.caseinfo.CaseInfoMappingService;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.*;
import no.fintlabs.model.SourceApplicationIdAndSourceApplicationIntegrationId;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.fint.Integration;
import no.fintlabs.model.fint.caseinfo.AdministrativeUnit;
import no.fintlabs.model.fint.caseinfo.CaseInfo;
import no.fintlabs.model.fint.caseinfo.CaseManager;
import no.fintlabs.model.fint.caseinfo.CaseStatus;
import no.fintlabs.model.fint.instance.Instance;
import no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil;
import no.fintlabs.validation.AcosInstanceValidationService;
import no.fintlabs.validation.InstanceValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;
import static no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil.getSourceApplicationId;

@RestController
@RequestMapping(EXTERNAL_API + "/acos/instanser")
public class AcosInstanceController {

    private final AcosInstanceValidationService acosInstanceValidationService;
    private final AcosInstanceMapper acosInstanceMapper;
    private final ReceivedInstanceEventProducerService receivedInstanceEventProducerService;
    private final InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService;
    private final IntegrationRequestProducerService integrationRequestProducerService;
    private final ArchiveCaseIdRequestService archiveCaseIdRequestService;
    private final ArchiveCaseRequestService archiveCaseRequestService;
    private final CaseInfoMappingService caseInfoMappingService;

    public AcosInstanceController(
            AcosInstanceValidationService acosInstanceValidationService,
            AcosInstanceMapper acosInstanceMapper,
            ReceivedInstanceEventProducerService receivedInstanceEventProducerService,
            InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService,
            IntegrationRequestProducerService integrationRequestProducerService,
            ArchiveCaseIdRequestService archiveCaseIdRequestService,
            ArchiveCaseRequestService archiveCaseRequestService,
            CaseInfoMappingService caseInfoMappingService
    ) {
        this.acosInstanceValidationService = acosInstanceValidationService;
        this.acosInstanceMapper = acosInstanceMapper;
        this.receivedInstanceEventProducerService = receivedInstanceEventProducerService;
        this.instanceReceivalErrorEventProducerService = instanceReceivalErrorEventProducerService;
        this.integrationRequestProducerService = integrationRequestProducerService;
        this.archiveCaseIdRequestService = archiveCaseIdRequestService;
        this.archiveCaseRequestService = archiveCaseRequestService;
        this.caseInfoMappingService = caseInfoMappingService;
    }

    @GetMapping("{sourceApplicationInstanceId}/saksinformasjon")
    public Mono<ResponseEntity<CaseInfo>> getInstanceCaseInfo(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @PathVariable String sourceApplicationInstanceId,
            @RequestParam Optional<Boolean> returnMockData
    ) {
        return authenticationMono.map(authentication -> getCaseInfo(
                authentication,
                sourceApplicationInstanceId,
                returnMockData.orElse(false)
        ));
    }

    public ResponseEntity<CaseInfo> getCaseInfo(
            Authentication authentication,
            String sourceApplicationInstanceId,
            boolean returnMockData
    ) {
        if (returnMockData) {
            return ResponseEntity.ok(createMockCaseInfo(sourceApplicationInstanceId));
        }
        return archiveCaseIdRequestService.getArchiveCaseId(
                        getSourceApplicationId(authentication),
                        sourceApplicationInstanceId
                )
                .flatMap(archiveCaseRequestService::getByArchiveCaseId)
                .map(caseResource -> caseInfoMappingService.toCaseInfo(sourceApplicationInstanceId, caseResource))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Case with sourceApplicationInstanceId=%s could not be found", sourceApplicationInstanceId)
                ));
    }

    private CaseInfo createMockCaseInfo(String sourceApplicationInstanceId) {
        return CaseInfo
                .builder()
                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                .archiveCaseId("2021/02")
                .caseManager(
                        CaseManager
                                .builder()
                                .firstName("Ola")
                                .middleName(null)
                                .lastName("Nordmann")
                                .email("ola.normann@domain.com")
                                .phone("12345678")
                                .build()
                )
                .administrativeUnit(
                        AdministrativeUnit
                                .builder()
                                .name("VGGLEM Skolemiljø og Kommunikasjon")
                                .build()
                )
                .status(
                        CaseStatus
                                .builder()
                                .name("Under behandling")
                                .code("B")
                                .build()
                )
                .build();
    }

    @PostMapping
    public Mono<ResponseEntity<?>> postInstance(
            @RequestBody AcosInstance acosInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono) {

        return authenticationMono.map(authentication -> processInstance(acosInstance, authentication));
    }

    private ResponseEntity<?> processInstance(AcosInstance acosInstance, Authentication authentication) {

        InstanceFlowHeaders.InstanceFlowHeadersBuilder instanceFlowHeadersBuilder = InstanceFlowHeaders.builder();

        try {
            Long sourceApplicationId = ClientAuthorizationUtil.getSourceApplicationId(authentication);

            instanceFlowHeadersBuilder.correlationId(UUID.randomUUID());
            instanceFlowHeadersBuilder.sourceApplicationId(sourceApplicationId);
            if (acosInstance.getMetadata() != null) {

                String sourceApplicationIntegrationId = acosInstance.getMetadata().getFormId();

                instanceFlowHeadersBuilder.sourceApplicationIntegrationId(sourceApplicationIntegrationId);
                instanceFlowHeadersBuilder.sourceApplicationInstanceId(acosInstance.getMetadata().getInstanceId());


                if (!acosInstance.getMetadata().getFormId().isBlank()) {

                    SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId =
                            SourceApplicationIdAndSourceApplicationIntegrationId
                                    .builder()
                                    .sourceApplicationId(sourceApplicationId)
                                    .sourceApplicationIntegrationId(sourceApplicationIntegrationId)
                                    .build();

                    Integration integration = integrationRequestProducerService
                            .get(sourceApplicationIdAndSourceApplicationIntegrationId)
                            .orElseThrow(() -> new NoIntegrationException(sourceApplicationIdAndSourceApplicationIntegrationId));

                    if (integration.getState() == Integration.State.DEACTIVATED) {
                        throw new IntegrationDeactivatedException(integration);
                    }

                    instanceFlowHeadersBuilder.integrationId(integration.getId());
                }
            }

            acosInstanceValidationService.validate(acosInstance).ifPresent((validationErrors) -> {
                throw new InstanceValidationException(validationErrors);
            });

            Instance instance = acosInstanceMapper.toInstance(acosInstance);

            receivedInstanceEventProducerService.publish(
                    instanceFlowHeadersBuilder.build(),
                    instance
            );

            return ResponseEntity.accepted().build();

        } catch (InstanceValidationException e) {
            instanceReceivalErrorEventProducerService.publishInstanceValidationErrorEvent(instanceFlowHeadersBuilder.build(), e);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Validation error" + (e.getValidationErrors().size() > 1 ? "s:" : ": ") +
                            e.getValidationErrors()
                                    .stream()
                                    .map(error -> "'" + error.getFieldPath() + " " + error.getErrorMessage() + "'")
                                    .toList()
            );
        } catch (NoIntegrationException e) {
            instanceReceivalErrorEventProducerService.publishNoIntegrationFoundErrorEvent(instanceFlowHeadersBuilder.build(), e);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    e.getMessage()
            );
        }  catch (IntegrationDeactivatedException e) {
            instanceReceivalErrorEventProducerService.publishIntegrationDeactivatedErrorEvent(instanceFlowHeadersBuilder.build(), e);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    e.getMessage()
            );
        }catch (RuntimeException e) {
            instanceReceivalErrorEventProducerService.publishGeneralSystemErrorEvent(instanceFlowHeadersBuilder.build());
            throw e;
        }

    }

}
