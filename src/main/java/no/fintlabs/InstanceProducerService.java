package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InstanceProducerService {

    private final EventProducer<Object> instanceProducer;
    private final EventTopicNameParameters formDefinitionEventTopicNameParameters;

    public InstanceProducerService(
            EventProducerFactory eventProducerFactory,
            EventTopicService eventTopicService
    ) {
        this.instanceProducer = eventProducerFactory.createProducer(Object.class);
        this.formDefinitionEventTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("incoming-instance")
                .build();
        eventTopicService.ensureTopic(formDefinitionEventTopicNameParameters, 15778463000L);
    }

    public void publishNewIntegrationMetadata(Object instance) {
        instanceProducer.send(
                EventProducerRecord.builder()
                        .topicNameParameters(formDefinitionEventTopicNameParameters)
                        .value(instance)
                        .build()
        );
    }

}
