package no.fintlabs;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.model.SourceApplicationIdAndSourceApplicationIntegrationId;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.fint.Instance;
import no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil;
import no.fintlabs.validation.AcosInstanceValidationService;
import no.fintlabs.validation.InstanceValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/instans/acos")
public class AcosInstanceController {

    private final AcosInstanceValidationService acosInstanceValidationService;
    private final AcosInstanceMapper acosInstanceMapper;
    private final ReceivedInstanceEventProducerService receivedInstanceEventProducerService;
    private final InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService;
    private final IntegrationIdRequestProducerService integrationIdRequestProducerService;

    public AcosInstanceController(
            AcosInstanceValidationService acosInstanceValidationService,
            AcosInstanceMapper acosInstanceMapper,
            ReceivedInstanceEventProducerService receivedInstanceEventProducerService,
            InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService,
            IntegrationIdRequestProducerService integrationIdRequestProducerService) {
        this.acosInstanceValidationService = acosInstanceValidationService;
        this.acosInstanceMapper = acosInstanceMapper;
        this.receivedInstanceEventProducerService = receivedInstanceEventProducerService;
        this.instanceReceivalErrorEventProducerService = instanceReceivalErrorEventProducerService;
        this.integrationIdRequestProducerService = integrationIdRequestProducerService;
    }

    @PostMapping()
    public Mono<ResponseEntity<?>> postInstance(
            @RequestBody AcosInstance acosInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono) {

        return authenticationMono.map(authentication -> processInstance(acosInstance, authentication));
    }

    private ResponseEntity<?> processInstance(AcosInstance acosInstance, Authentication authentication) {

        InstanceFlowHeaders.InstanceFlowHeadersBuilder instanceFlowHeadersBuilder = InstanceFlowHeaders.builder();

        try {
            Long sourceApplicationId = ClientAuthorizationUtil.getSourceApplicationId(authentication);

            instanceFlowHeadersBuilder.correlationId(UUID.randomUUID().toString());
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

                    Long integrationId = integrationIdRequestProducerService
                            .get(sourceApplicationIdAndSourceApplicationIntegrationId)
                            .orElseThrow(() -> new NoIntegrationException(sourceApplicationIdAndSourceApplicationIntegrationId));

                    instanceFlowHeadersBuilder.integrationId(integrationId);

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
        } catch (RuntimeException e) {
            instanceReceivalErrorEventProducerService.publishGeneralSystemErrorEvent(instanceFlowHeadersBuilder.build());
            throw e;
        }

    }

}
