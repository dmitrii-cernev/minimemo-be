package md.cernev.minimemo.service;

import md.cernev.minimemo.repository.VideosRepository;
import md.cernev.minimemo.scrapper.Scrapper;
import md.cernev.minimemo.util.Platform;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.UUID;

@Service
public class PipelineService {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PipelineService.class);
  private final WebClient webClient;
  private final Scrapper scrapper;
  private final VideosRepository miniMemoRepository;
  @Value("${aws.lambda.scrapper.host}")
  private String lambdaHost;

  public PipelineService(WebClient webClient, Scrapper scrapper, VideosRepository miniMemoRepository) {
    this.webClient = webClient;
    this.scrapper = scrapper;
    this.miniMemoRepository = miniMemoRepository;
  }

  public Mono<String> startPipeline(String url, String userId) {
    Platform platform = getPlatform(url);
    logger.info("Starting pipeline for url: {}", url);
    //todo: this method is blocking
    String videoId = UUID.randomUUID().toString();
    Mono<PutItemResponse> putItem = Mono.fromFuture(() -> miniMemoRepository.putItem(userId, videoId, url, platform));
    Mono<String> summary = getSummary(userId, videoId, url, platform);
    return Mono.when(putItem, summary).thenReturn(videoId);
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
    return scrapper.getDownloadLink(url, platform)
        .flatMap(downloadLink -> webClient
            .post()
            .uri(lambdaHost + "/process")
            .bodyValue(getRequestBody(userId, videoId, downloadLink).toString())
            .retrieve()
            .bodyToMono(String.class));
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
