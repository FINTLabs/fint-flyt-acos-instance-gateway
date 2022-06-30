package no.fintlabs;

import no.vigoiks.resourceserver.security.FintJwtUserConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${fint.integration.service.authorized-org-id:viken.no}")
    private String authorizedOrgId;

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            @Value("${fint.security.resourceserver.enabled:true}") boolean enabled
    ) {
        return enabled
                ? createOauth2FilterChain(http)
                : createPermitAllFilterChain(http);
    }

    private SecurityWebFilterChain createOauth2FilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/api/admin/**").access(this::manageAdminAccess)
                        .pathMatchers("/api/intern/**").hasAnyAuthority("ORGID_" + authorizedOrgId, "ORGID_vigo.no")
                        .pathMatchers("/api/**").access(this::manageExternalAccess)
                        .pathMatchers("/**").denyAll()
                        .anyExchange()
                        .authenticated())
                .addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .oauth2ResourceServer((resourceServer) -> resourceServer
                                .jwt()
                                .jwtAuthenticationConverter(new FintJwtUserConverter())
                        // TODO: 29/06/2022 Kunne vi hatt en FintJwtClientConverter som sjekker sub-claimen mot
                        //  klient-autentiseringstjenesten og legger til resultatet i authorities -- feks "CLIENT_ACOS"?
                        //  Vi kan vel skille mellom person og maskin på scope, og kan velge converter avhengig av det?
                )
                .build();
    }

    private Mono<AuthorizationDecision> manageAdminAccess(Mono<Authentication> mono, Object context) {
        return mono.map(authentication -> authentication
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet())
                        .containsAll(asList(
                                "ORGID_vigo.no"
//                        , "ROLE_fint.flyt.admin" // TODO: 29/06/2022 Add support for this role
                        ))
        ).map(AuthorizationDecision::new);
    }

    private Mono<AuthorizationDecision> manageExternalAccess(Mono<Authentication> mono, Object context) {
        return mono
                .map((auth) -> auth instanceof JwtAuthenticationToken
                        && ((JwtAuthenticationToken) auth)
                        .getTokenAttributes()
                        .get("sub")
                        .equals("5679f546-b72e-41d4-bbfe-68b029a8c158")
                )
                .map(AuthorizationDecision::new);
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
