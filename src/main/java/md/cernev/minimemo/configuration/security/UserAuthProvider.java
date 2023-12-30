package md.cernev.minimemo.configuration.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.service.UserService;
import md.cernev.minimemo.util.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
        try {
            DecodedJWT decodedJWT = getDecodedJWT(token);

            return userService.findByLogin(decodedJWT.getIssuer())
                .map(userDto -> new UsernamePasswordAuthenticationToken(userDto, null, Collections.emptyList()));
        } catch (IllegalArgumentException e) {
            return Mono.error(new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST));
        } catch (JWTVerificationException e) {
            return Mono.error(new CustomException(e.getMessage(), HttpStatus.UNAUTHORIZED));
        }
    }

    public DecodedJWT getDecodedJWT(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
            .build();
        return verifier.verify(token);
    }

    public String getIssuer(String token) {
        return getDecodedJWT(token.split(" ")[1]).getIssuer();
    }

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
}