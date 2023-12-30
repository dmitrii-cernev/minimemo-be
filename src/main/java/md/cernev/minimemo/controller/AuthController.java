package md.cernev.minimemo.controller;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.configuration.security.UserAuthProvider;
import md.cernev.minimemo.dto.CredentialsDto;
import md.cernev.minimemo.dto.SignUpDto;
import md.cernev.minimemo.dto.UserDto;
import md.cernev.minimemo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    @PostMapping("/login")
    public Mono<ResponseEntity<UserDto>> login(@RequestBody CredentialsDto credentialsDto) {
        Mono<UserDto> login = userService.login(credentialsDto);
        return login.map(userDto -> {
            userDto.setToken(userAuthProvider.createToken(userDto.getLogin()));
            return ResponseEntity.ok(userDto);
        });
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<UserDto>> register(@RequestBody SignUpDto signUpDto) {
        Mono<UserDto> register = userService.register(signUpDto);
        return register.map(userDto -> {
            userDto.setToken(userAuthProvider.createToken(userDto.getLogin()));
            return ResponseEntity.created(URI.create("/users/" + userDto.getId())).body(userDto);
        });
    }
}
