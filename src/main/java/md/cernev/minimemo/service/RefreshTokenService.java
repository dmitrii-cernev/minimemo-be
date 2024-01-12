package md.cernev.minimemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.cernev.minimemo.configuration.security.UserAuthProvider;
import md.cernev.minimemo.dto.UserDto;
import md.cernev.minimemo.entity.RefreshToken;
import md.cernev.minimemo.entity.User;
import md.cernev.minimemo.repository.RefreshTokenRepository;
import md.cernev.minimemo.repository.UserRepository;
import md.cernev.minimemo.util.CustomHttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserAuthProvider userAuthProvider;
    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;

    public CompletableFuture<String> createToken(String userId, String userLogin) {
        log.info("Creating refresh token for user {}", userLogin);
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(userId)
            .refreshToken(UUID.randomUUID().toString())
            .userLogin(userLogin)
            .expiration(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(refreshTokenExpiration).toString())
            .build();
        return refreshTokenRepository.save(refreshToken).thenApply(RefreshToken::getRefreshToken);
    }

    public CompletableFuture<UserDto> refreshToken(String login, String refreshToken) {
        log.info("Refreshing token {}", refreshToken);
        CompletableFuture<User> userFuture = userRepository.findByLogin(login)
            .thenApply(user -> {
                if (user.isEmpty()) {
                    throw new CustomHttpException("User not found", HttpStatus.UNAUTHORIZED);
                }
                return user.get();
            });
        return userFuture.thenCompose(user -> refreshTokenByUserId(user.getId(), refreshToken));
    }

    private CompletableFuture<UserDto> refreshTokenByUserId(String userId, String refreshToken) {
        return refreshTokenRepository.findByToken(userId, refreshToken)
            .thenApply(rt -> {
                if (rt.isEmpty()) {
                    throw new CustomHttpException("Refresh token not found", HttpStatus.UNAUTHORIZED);
                }
                var token = rt.get();
                if (isExpired(token.getExpiration())) {
                    refreshTokenRepository.delete(token);
                    throw new CustomHttpException("Refresh token expired", HttpStatus.UNAUTHORIZED);
                }
                String accessToken = userAuthProvider.createToken(token.getUserLogin(), userId);
                String newRefreshToken = UUID.randomUUID().toString();
                String newExpiration = ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(refreshTokenExpiration)
                    .toString();
                RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                    .userId(token.getUserId())
                    .userLogin(token.getUserLogin())
                    .refreshToken(newRefreshToken)
                    .expiration(newExpiration)
                    .build();
                refreshTokenRepository.save(newRefreshTokenEntity);
                return UserDto.builder()
                    .token(accessToken)
                    .refreshToken(newRefreshToken)
                    .build();
            });
    }

    private boolean isExpired(String expiration) {
        return ZonedDateTime.parse(expiration).isBefore(ZonedDateTime.now(ZoneId.systemDefault()));
    }
}
