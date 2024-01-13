package md.cernev.minimemo.controller;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.configuration.security.UserAuthProvider;
import md.cernev.minimemo.dto.CountDto;
import md.cernev.minimemo.service.SubscriptionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final UserAuthProvider userAuthProvider;

    @GetMapping("/subscription/count")
    public Mono<CountDto> getSubscriptionsCount(@RequestHeader(name = "Authorization") String token) {
        String userId = userAuthProvider.getUserId(token);
        return subscriptionService.getSubscriptionsCount(userId);
    }

    @GetMapping("/open/subscription/count/{userId}")
    public Mono<CountDto> getSubscriptionsCountOpen(@PathVariable String userId) {
        return subscriptionService.getSubscriptionsCount(userId);
    }

}
