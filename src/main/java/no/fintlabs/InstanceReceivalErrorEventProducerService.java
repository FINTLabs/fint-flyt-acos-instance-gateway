package no.fintlabs;

import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducer;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicService;
import org.springframework.stereotype.Service;

@Service
public class InstanceReceivalErrorEventProducerService {

    private final InstanceFlowErrorEventProducer instanceFlowErrorEventProducer;
    private final ErrorEventTopicNameParameters instanceProcessingErrorTopicNameParameters;

    public InstanceReceivalErrorEventProducerService(
            ErrorEventTopicService errorEventTopicService,
            InstanceFlowErrorEventProducer instanceFlowErrorEventProducer
    ) {
        this.instanceFlowErrorEventProducer = instanceFlowErrorEventProducer;

        this.instanceProcessingErrorTopicNameParameters = ErrorEventTopicNameParameters.builder()
                .errorEventName("instance-receival-error")
                .build();

        errorEventTopicService.ensureTopic(instanceProcessingErrorTopicNameParameters, 0);
    }

    public void publishGeneralSystemErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(new ErrorCollection(Error
                                .builder()
                                .errorCode(ErrorCode.GENERAL_SYSTEM_ERROR.getCode())
                                .build()))
                        .build()
        );
    }

}
