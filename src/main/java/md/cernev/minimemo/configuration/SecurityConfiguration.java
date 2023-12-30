package md.cernev.minimemo.configuration;

import lombok.RequiredArgsConstructor;
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
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        //todo: fix deprecated methods
        return http
            .exceptionHandling()
            .authenticationEntryPoint(userAuthenticationEntryPoint)
            .and()
            .csrf().disable()
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange()
            .pathMatchers("/login", "/register", "/ping").permitAll()
            .anyExchange().authenticated()
            .and()
            .addFilterBefore(new JwtAuthFilter(userAuthProvider), SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }
}
