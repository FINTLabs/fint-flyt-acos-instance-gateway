package no.fintlabs.security;

import no.fintlabs.security.client.ClientJwtConverter;
import no.vigoiks.resourceserver.security.FintJwtUserConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

import static no.fintlabs.security.client.ClientAuthorizationUtil.SOURCE_APPLICATION_ID_PREFIX;

@EnableWebFluxSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    @Value("${fint.flyt.security.api.internal.authorized-org-id}")
    private String internalApiAuthorizedOrgId;

    @Value("${fint.flyt.security.api.external.authorized-client-id}")
    private String externalApiAuthorizedClientId;

    private final ClientJwtConverter clientJwtConverter;

    public SecurityConfiguration(ClientJwtConverter clientJwtConverter) {
        this.clientJwtConverter = clientJwtConverter;
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    SecurityWebFilterChain externalAccessFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/**"))
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt()
                        .jwtAuthenticationConverter(clientJwtConverter)
                )
                .authorizeExchange()
                .anyExchange()
                .hasAuthority(SOURCE_APPLICATION_ID_PREFIX + externalApiAuthorizedClientId)
                .and()
                .build();
    }

    @Bean
    SecurityWebFilterChain internalAccessFilterChain(
            ServerHttpSecurity http,
            @Value("${fint.security.resourceserver.enabled:true}") boolean enabled
    ) {
        return enabled
                ? createSecuredFilterChain(http)
                : createPermitAllFilterChain(http);
    }

    private SecurityWebFilterChain createSecuredFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
                .pathMatchers("/api/admin/**")
                .hasAuthority("ORGID_vigo.no"); // TODO: 17/08/2022 ROLE_admin

        http
                .authorizeExchange()
                .pathMatchers("/api/intern/**")
                .hasAnyAuthority("ORGID_" + internalApiAuthorizedOrgId, "ORGID_vigo.no");

        http
                .authorizeExchange()
                .pathMatchers("/**")
                .denyAll();

        return http
                .addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt()
                        .jwtAuthenticationConverter(new FintJwtUserConverter())
                )
                .build();
    }

    private SecurityWebFilterChain createPermitAllFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                .anyExchange()
                .permitAll()
                .and()
                .build();
    }

}
