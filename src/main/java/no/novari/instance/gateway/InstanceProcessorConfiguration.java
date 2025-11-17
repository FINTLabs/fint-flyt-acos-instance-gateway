package no.novari.instance.gateway;

import no.novari.flyt.instance.gateway.InstanceProcessor;
import no.novari.flyt.instance.gateway.InstanceProcessorFactoryService;
import no.novari.instance.gateway.model.acos.AcosInstance;
import no.novari.instance.gateway.model.acos.AcosInstanceMetadata;
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
