package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.caseinfo.CaseInfoMappingService;
import no.fintlabs.gateway.instance.ArchiveCaseService;
import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.caseinfo.CaseInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

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
                .doOnNext(authentication -> log.info("Received instance: " + acosInstance))
                .flatMap(authentication -> instanceProcessor.processInstance(authentication, acosInstance));
    }

}
