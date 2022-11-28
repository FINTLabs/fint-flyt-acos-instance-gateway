package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.InstanceProcessorFactoryService;
import no.fintlabs.model.acos.AcosInstance;
import no.fintlabs.model.acos.AcosInstanceMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class InstanceProcessorConfiguration {

    @Bean
    public InstanceProcessor<AcosInstance> instanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            AcosInstanceMapper acosInstanceMapper
    ) {
        return instanceProcessorFactoryService.createInstanceProcessor(
                acosInstance -> Optional.ofNullable(acosInstance.getMetadata()).map(AcosInstanceMetadata::getFormId),
                acosInstance -> Optional.ofNullable(acosInstance.getMetadata()).map(AcosInstanceMetadata::getInstanceId),
                acosInstanceMapper
        );
    }

}
