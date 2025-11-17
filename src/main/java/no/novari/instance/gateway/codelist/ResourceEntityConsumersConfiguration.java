package no.novari.instance.gateway.codelist;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.kodeverk.SaksstatusResource;
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.fint.model.resource.felles.PersonResource;
import no.novari.cache.FintCache;
import no.novari.instance.gateway.codelist.links.ResourceLinkUtil;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.requestreply.ReplyProducerRecord;
import no.novari.kafka.requestreply.RequestListenerConfiguration;
import no.novari.kafka.requestreply.RequestListenerContainerFactory;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ResourceEntityConsumersConfiguration {

    private final RequestListenerContainerFactory requestListenerContainerFactory;
    private final ErrorHandlerFactory errorHandlerFactory;

    public ResourceEntityConsumersConfiguration(
            RequestListenerContainerFactory requestListenerContainerFactory,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        this.requestListenerContainerFactory = requestListenerContainerFactory;
        this.errorHandlerFactory = errorHandlerFactory;
    }

    private <T extends FintLinks> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceReference,
            Class<T> resourceClass,
            FintCache<String, T> cache
    ) {
        return requestListenerContainerFactory.createRecordConsumerFactory(
                resourceClass,
                Void.class,
                (ConsumerRecord<String, T> record) -> {
                    cache.put(ResourceLinkUtil.getSelfLinks(record.value()), record.value());
                    return ReplyProducerRecord.<Void>builder().build();
                },
                RequestListenerConfiguration
                        .stepBuilder(resourceClass)
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .build(),
                errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                        .stepBuilder()
                        .noRetries()
                        .skipFailedRecords()
                        .build())

        ).createContainer(EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName(resourceReference)
                .build()
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, AdministrativEnhetResource> administrativEnhetResourceEntityConsumer(
            FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-noark-administrativenhet",
                AdministrativEnhetResource.class,
                administrativEnhetResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ArkivressursResource> arkivressursResourceEntityConsumer(
            FintCache<String, ArkivressursResource> arkivressursResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-noark-arkivressurs",
                ArkivressursResource.class,
                arkivressursResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, SaksstatusResource> saksstatusResourceEntityConsumer(
            FintCache<String, SaksstatusResource> saksstatusResourceCache
    ) {
        return createCacheConsumer(
                "arkiv-kodeverk-saksstatus",
                SaksstatusResource.class,
                saksstatusResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, PersonalressursResource> personalressursResourceEntityConsumer(
            FintCache<String, PersonalressursResource> personalressursResourceCache
    ) {
        return createCacheConsumer(
                "administrasjon-personal-personalressurs",
                PersonalressursResource.class,
                personalressursResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, PersonResource> personResourceEntityConsumer(
            FintCache<String, PersonResource> personResourceCache
    ) {
        return createCacheConsumer(
                "administrasjon-personal-person",
                PersonResource.class,
                personResourceCache
        );
    }

}
