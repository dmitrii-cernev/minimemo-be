package md.cernev.minimemo.configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class UserAuthProvider {
    private final UserService userService;
    @Value("${security.jwt.token.secret:secret}")
    private String secretKey;

    public String createToken(String login) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + 3_600_000);
        return JWT.create()
            .withIssuer(login)
            .withIssuedAt(now)
            .withExpiresAt(expiredAt)
            .sign(Algorithm.HMAC256(secretKey));
    }

    public Mono<Authentication> validateToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
            .build();
        DecodedJWT decodedJWT = verifier.verify(token);

        return userService.findByLogin(decodedJWT.getIssuer())
            .map(userDto -> new UsernamePasswordAuthenticationToken(userDto, null, Collections.emptyList()));
    }

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
}
