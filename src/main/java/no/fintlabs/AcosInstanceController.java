package no.fintlabs;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.fint.Instance;
import no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil;
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

import java.util.List;
import java.util.UUID;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/instans/acos")
public class AcosInstanceController {

    @Value("${fint.org-id}")
    private String orgId;
    private final AcosInstanceValidator acosInstanceValidator;
    private final AcosInstanceMapper acosInstanceMapper;
    private final ReceivedInstanceEventProducerService receivedInstanceEventProducerService;
    private final InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService;

    public AcosInstanceController(
            AcosInstanceValidator acosInstanceValidator,
            AcosInstanceMapper acosInstanceMapper,
            ReceivedInstanceEventProducerService receivedInstanceEventProducerService,
            InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService
    ) {
        this.acosInstanceValidator = acosInstanceValidator;
        this.acosInstanceMapper = acosInstanceMapper;
        this.receivedInstanceEventProducerService = receivedInstanceEventProducerService;
        this.instanceReceivalErrorEventProducerService = instanceReceivalErrorEventProducerService;
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
            instanceFlowHeadersBuilder.orgId(orgId);
            instanceFlowHeadersBuilder.correlationId(UUID.randomUUID().toString());
            instanceFlowHeadersBuilder.sourceApplicationId(ClientAuthorizationUtil.getSourceApplicationId(authentication));
            instanceFlowHeadersBuilder.sourceApplicationIntegrationId(acosInstance.getMetadata().getFormId());
            instanceFlowHeadersBuilder.sourceApplicationInstanceId(acosInstance.getMetadata().getInstanceId());

            acosInstanceValidator.validate(acosInstance).ifPresent(
                    (List<String> validationErrors) -> {
                        throw new ResponseStatusException(
                                HttpStatus.UNPROCESSABLE_ENTITY, "Validation error(s): "
                                + validationErrors.stream().map(error -> "'" + error + "'").toList()
                        );
                    }
            );
            Instance instance = acosInstanceMapper.toInstance(acosInstance);

            receivedInstanceEventProducerService.publish(
                    instanceFlowHeadersBuilder.build(),
                    instance
            );
            return ResponseEntity.accepted().build();
        } catch (RuntimeException e) {
            instanceReceivalErrorEventProducerService.publishGeneralSystemErrorEvent(instanceFlowHeadersBuilder.build());
            throw e;
        }

    }

}
