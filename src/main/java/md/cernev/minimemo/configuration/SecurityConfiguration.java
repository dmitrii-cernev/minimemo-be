package md.cernev.minimemo.configuration;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.configuration.security.JwtAuthFilter;
import md.cernev.minimemo.configuration.security.UserAuthProvider;
import md.cernev.minimemo.configuration.security.UserAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final UserAuthProvider userAuthProvider;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
            .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(userAuthenticationEntryPoint))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange(authorizeExchange -> authorizeExchange
                .pathMatchers("/login", "/register", "/api/open/**", "/actuator/**").permitAll().anyExchange()
                .authenticated())
            .addFilterBefore(new JwtAuthFilter(userAuthProvider), SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }
}
