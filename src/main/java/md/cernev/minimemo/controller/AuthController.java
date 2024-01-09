package md.cernev.minimemo.controller;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.configuration.security.UserAuthProvider;
import md.cernev.minimemo.dto.CredentialsDto;
import md.cernev.minimemo.dto.RefreshTokenRequestDto;
import md.cernev.minimemo.dto.SignUpDto;
import md.cernev.minimemo.dto.UserDto;
import md.cernev.minimemo.service.RefreshTokenService;
import md.cernev.minimemo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserAuthProvider userAuthProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public Mono<ResponseEntity<UserDto>> login(@RequestBody CredentialsDto credentialsDto) {
        Mono<UserDto> login = userService.login(credentialsDto);
        return login.map(userDto -> {
            userDto.setToken(userAuthProvider.createToken(userDto.getLogin()));
            CompletableFuture<String> refreshToken = refreshTokenService.createToken(userDto.getLogin());
            //todo: blockalbe?
            userDto.setRefreshToken(refreshToken.join());
            return ResponseEntity.ok(userDto);
        });
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<UserDto>> register(@RequestBody SignUpDto signUpDto) {
        Mono<UserDto> register = userService.register(signUpDto);
        return register.map(userDto -> {
            userDto.setToken(userAuthProvider.createToken(userDto.getLogin()));
            CompletableFuture<String> refreshToken = refreshTokenService.createToken(userDto.getLogin());
            //todo: blockalbe?
            userDto.setRefreshToken(refreshToken.join());
            return ResponseEntity.created(URI.create("/users/" + userDto.getId())).body(userDto);
        });
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<UserDto>> refresh(@RequestBody RefreshTokenRequestDto refreshToken) {
        //todo: also send expired, but valid access token. Should be not very old.
        CompletableFuture<UserDto> userDtoCompletableFuture = refreshTokenService.refreshToken(refreshToken.getRefreshToken());
        return Mono.fromFuture(userDtoCompletableFuture).map(ResponseEntity::ok);

    }
}
