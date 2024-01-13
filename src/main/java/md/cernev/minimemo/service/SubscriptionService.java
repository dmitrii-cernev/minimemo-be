package md.cernev.minimemo.service;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.dto.CountDto;
import md.cernev.minimemo.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public Mono<CountDto> getSubscriptionsCount(String userId) {
        return subscriptionRepository.getOrCreateSubscriptionsCount(userId);
    }

    public Mono<CountDto> decrementCount(String userId) {
        return subscriptionRepository.decrementCount(userId);
    }
}
