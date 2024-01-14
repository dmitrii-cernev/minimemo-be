package md.cernev.minimemo.controller;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.configuration.security.UserAuthProvider;
import md.cernev.minimemo.dto.UserDto;
import md.cernev.minimemo.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    @GetMapping("/auth/user")
    public Mono<UserDto> getUser(@RequestHeader(name = "Authorization") String token) {
        String userId = userAuthProvider.getUserId(token);
        String login = userAuthProvider.getIssuer(token);
        return userService.findByUserId(userId, login);
    }

}
