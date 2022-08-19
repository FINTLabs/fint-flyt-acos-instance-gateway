package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducer;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerFactory;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.model.fint.Instance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class InstanceProducerService {

    @Value("${fint.org-id}")
    private String orgId;
    private final InstanceFlowEventProducer<Instance> instanceProducer;
    private final EventTopicNameParameters formDefinitionEventTopicNameParameters;

    public InstanceProducerService(
            InstanceFlowEventProducerFactory instanceFlowEventProducerFactory,
            EventTopicService eventTopicService
    ) {
        this.instanceProducer = instanceFlowEventProducerFactory.createProducer(Instance.class);
        this.formDefinitionEventTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("incoming-instance")
                .build();
        eventTopicService.ensureTopic(formDefinitionEventTopicNameParameters, 15778463000L);
    }

    public void publishIncomingInstance(
            String sourceApplicationId,
            String sourceApplicationIntegrationId,
            String sourceApplicationInstanceId,
            Instance instance
    ) {
        instanceProducer.send(
                InstanceFlowEventProducerRecord.<Instance>builder()
                        .topicNameParameters(formDefinitionEventTopicNameParameters)
                        .instanceFlowHeaders(InstanceFlowHeaders
                                .builder()
                                .orgId(orgId)
                                .sourceApplicationId(sourceApplicationId)
                                .sourceApplicationIntegrationId(sourceApplicationIntegrationId)
                                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                                .correlationId(UUID.randomUUID().toString())
                                .build()
                        )
                        .value(instance)
                        .build()
        );
    }

}
