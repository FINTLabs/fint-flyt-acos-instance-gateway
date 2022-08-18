package no.fintlabs;

import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.fint.Instance;
import no.fintlabs.security.client.ClientAuthorizationUtil;
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

@RestController
@RequestMapping("api/instans/acos")
public class AcosInstanceController {

    private final AcosInstanceValidator acosInstanceValidator;
    private final AcosInstanceMapper acosInstanceMapper;
    private final InstanceProducerService instanceProducerService;

    public AcosInstanceController(
            AcosInstanceValidator acosInstanceValidator,
            AcosInstanceMapper acosInstanceMapper,
            InstanceProducerService instanceProducerService
    ) {
        this.acosInstanceValidator = acosInstanceValidator;
        this.acosInstanceMapper = acosInstanceMapper;
        this.instanceProducerService = instanceProducerService;
    }

    @PostMapping()
    public Mono<ResponseEntity<?>> postInstance(
            @RequestBody AcosInstance acosInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono) {
        return authenticationMono.map(authentication -> processInstance(acosInstance, authentication));
    }

    private ResponseEntity<?> processInstance(AcosInstance acosInstance, Authentication authentication) {
        acosInstanceValidator.validate(acosInstance).ifPresent(
                (List<String> validationErrors) -> {
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY, "Validation error(s): "
                            + validationErrors.stream().map(error -> "'" + error + "'").toList()
                    );
                }
        );
        Instance instance = acosInstanceMapper.toInstance(acosInstance);

        instanceProducerService.publishIncomingInstance(
                ClientAuthorizationUtil.getSourceApplicationId(authentication),
                acosInstance.getMetadata().getFormId(),
                acosInstance.getMetadata().getInstanceId(),
                instance
        );
        return ResponseEntity.accepted().build();
    }

}
