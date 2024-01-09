package md.cernev.minimemo.configuration.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {
    private final UserAuthProvider userAuthProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        List<String> authHeaders = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);

        if (!authHeaders.isEmpty()) {
            String[] parts = authHeaders.get(0).split(" ");
            if (parts.length == 2 && parts[0].equals("Bearer")) {
                return userAuthProvider.validateToken(parts[1])
                    .flatMap(authentication -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                    .onErrorResume(e -> {
                        //todo: may not work properly
                        ReactiveSecurityContextHolder.clearContext();
                        return Mono.error(e);
                    });
            }
        }
        return chain.filter(exchange);
    }
}
