package md.cernev.minimemo.service;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.repository.VideosRepository;
import md.cernev.minimemo.scrapper.Scrapper;
import md.cernev.minimemo.util.CustomHttpException;
import md.cernev.minimemo.util.Platform;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PipelineService {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PipelineService.class);
  private final WebClient webClient;
  private final Scrapper scrapper;
  private final VideosRepository miniMemoRepository;
  private final SubscriptionService subscriptionService;
  @Value("${aws.lambda.scrapper.host}")
  private String lambdaHost;

    public Mono<String> startPipeline(String url, String userId) {
        return subscriptionService.getSubscriptionsCount(userId)
            .flatMap(countDto -> {
                if (countDto.getCount() <= 0) {
                    logger.warn("No calls left for user: {}", userId);
                    return Mono.error(new CustomHttpException("No calls left", HttpStatus.BAD_REQUEST));
                }
                Platform platform = getPlatform(url);
                logger.info("Starting pipeline for user: {}", userId);
                String videoId = UUID.randomUUID().toString();
                Mono<PutItemResponse> putItem = Mono.fromFuture(() -> miniMemoRepository.putItem(userId, videoId, url, platform));
                Mono<String> summary = getSummary(userId, videoId, url, platform);
                return Mono.when(putItem, summary)
                    .thenReturn(videoId)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnCancel(() -> {
                        logger.info("Canceled pipeline for user: {}", userId);
                        getSummary(userId, videoId, url, platform).subscribe();
                    });
            });
    }

  private Platform getPlatform(String url) {
    if (url.contains("tiktok")) {
      return Platform.TIKTOK;
    } else if (url.contains("instagram")) {
      return Platform.INSTAGRAM;
    } else if (url.contains("shorts")) {
      return Platform.SHORTS;
    } else {
      throw new RuntimeException("Unsupported url");
    }
  }

    private Mono<String> getSummary(String userId, String videoId, String url, Platform platform) {
        logger.info("Requesting summary for user: {}", userId);
        return scrapper.getDownloadLink(url, platform)
            .flatMap(downloadLink -> webClient
                .post()
                .uri(lambdaHost + "/process")
                .bodyValue(getRequestBody(userId, videoId, downloadLink).toString())
                .retrieve()
                .bodyToMono(String.class))
            .doOnError(throwable -> {
                if (throwable instanceof WebClientResponseException.ServiceUnavailable) {
                    logger.warn("Video can be too long");
                    miniMemoRepository.updateItemStatus(userId, videoId, "TOO_LONG").join();
                    throw new CustomHttpException("Video can be too long", HttpStatus.REQUEST_TIMEOUT);
                }
                logger.error("Error while getting summary", throwable);
                miniMemoRepository.updateItemStatus(userId, videoId, "ERROR").join();
                throw new CustomHttpException("Error while getting summary", HttpStatus.INTERNAL_SERVER_ERROR);
            });
    }

  private JSONObject getRequestBody(String userId, String videoId, String url) {
    JSONObject requestBody = new JSONObject();
    requestBody.put("userId", userId);
    requestBody.put("subId", videoId);
    requestBody.put("url", url);
    return requestBody;
  }

  public Mono<Object> getVideoInfo(String videoId) {
    logger.info("Getting video info: {}", videoId);
    return Mono.fromFuture(miniMemoRepository.getItem(videoId));
  }

  public Mono<Object> getVideos(String userId) {
    logger.info("Getting video info: {}", userId);
    return Mono.fromFuture(miniMemoRepository.getItems(userId));
  }

  public Mono<String> ping() {
    return Mono.just("pong");
  }
}
