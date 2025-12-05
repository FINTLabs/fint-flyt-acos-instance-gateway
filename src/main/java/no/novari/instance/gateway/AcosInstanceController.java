package no.novari.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.instance.gateway.ArchiveCaseService;
import no.novari.flyt.instance.gateway.InstanceProcessor;
import no.novari.instance.gateway.caseinfo.CaseInfoMappingService;
import no.novari.instance.gateway.model.acos.AcosInstance;
import no.novari.instance.gateway.model.caseinfo.CaseInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static no.novari.flyt.resourceserver.UrlPaths.EXTERNAL_API;

@Slf4j
@RestController
@RequestMapping(EXTERNAL_API + "/acos/instances")
public class AcosInstanceController {

    private final InstanceProcessor<AcosInstance> instanceProcessor;
    private final ArchiveCaseService archiveCaseService;
    private final CaseInfoMappingService caseInfoMappingService;

    public AcosInstanceController(
            InstanceProcessor<AcosInstance> instanceProcessor,
            ArchiveCaseService archiveCaseService,
            CaseInfoMappingService caseInfoMappingService
    ) {
        this.instanceProcessor = instanceProcessor;
        this.archiveCaseService = archiveCaseService;
        this.caseInfoMappingService = caseInfoMappingService;
    }

    @GetMapping("{sourceApplicationInstanceId}/case-info")
    public Mono<ResponseEntity<CaseInfo>> getInstanceCaseInfo(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @PathVariable String sourceApplicationInstanceId
    ) {
        return authenticationMono.map(authentication -> getCaseInfo(
                authentication,
                sourceApplicationInstanceId
        ));
    }

    public ResponseEntity<CaseInfo> getCaseInfo(
            Authentication authentication,
            String sourceApplicationInstanceId
    ) {
        return archiveCaseService.getCase(authentication, sourceApplicationInstanceId)
                .map(caseResource -> caseInfoMappingService.toCaseInfo(sourceApplicationInstanceId, caseResource))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Case with sourceApplicationInstanceId=%s could not be found", sourceApplicationInstanceId)
                ));
    }

    @PostMapping
    public Mono<ResponseEntity<?>> postInstance(
            @RequestBody AcosInstance acosInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono) {
        return authenticationMono
                .doOnNext(authentication -> log.info("Received instance: {}", acosInstance))
                .flatMap(authentication -> instanceProcessor.processInstance(authentication, acosInstance));
    }

}
