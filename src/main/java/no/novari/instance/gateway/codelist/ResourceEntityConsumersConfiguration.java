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
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ResourceEntityConsumersConfiguration {

    private final ParameterizedListenerContainerFactoryService listenerFactoryService;
    private final ErrorHandlerFactory errorHandlerFactory;

    public ResourceEntityConsumersConfiguration(
            ParameterizedListenerContainerFactoryService listenerFactoryService,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        this.listenerFactoryService = listenerFactoryService;
        this.errorHandlerFactory = errorHandlerFactory;
    }

    private <T extends FintLinks> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceName,
            Class<T> resourceClass,
            FintCache<String, T> cache
    ) {
        ListenerConfiguration listenerConfig = ListenerConfiguration
                .stepBuilder()
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .seekToBeginningOnAssignment()
                .build();

        return listenerFactoryService
                .createRecordListenerContainerFactory(
                        resourceClass,
                        record -> {
                            T value = record.value();
                            cache.put(ResourceLinkUtil.getSelfLinks(value), value);
                        },
                        listenerConfig,
                        errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                                .stepBuilder()
                                .noRetries()
                                .skipFailedRecords()
                                .build())
                )
                .createContainer(
                        EntityTopicNameParameters.builder()
                                .topicNamePrefixParameters(
                                        TopicNamePrefixParameters.stepBuilder()
                                                .orgIdApplicationDefault()
                                                .domainContextApplicationDefault()
                                                .build()
                                )
                                .resourceName(resourceName)
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
