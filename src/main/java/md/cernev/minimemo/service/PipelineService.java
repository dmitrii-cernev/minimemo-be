package md.cernev.minimemo.service;

import md.cernev.minimemo.repository.MiniMemoRepository;
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
  private final MiniMemoRepository miniMemoRepository;
  @Value("${aws.lambda.scrapper.host}")
  private String lambdaHost;

  public PipelineService(WebClient webClient, Scrapper scrapper, MiniMemoRepository miniMemoRepository) {
    this.webClient = webClient;
    this.scrapper = scrapper;
    this.miniMemoRepository = miniMemoRepository;
  }

  public Mono<String> startPipeline(String url, String userId) {
    Platform platform = getPlatform(url);
    logger.info("Starting pipeline for url: {}", url);
    String videoId = UUID.randomUUID().toString();
    Mono<PutItemResponse> putItem = Mono.fromFuture(() -> miniMemoRepository.putItem(userId, videoId, url, platform));
    Mono<String> summary = getSummary(url, platform, videoId);
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

  private Mono<String> getSummary(String url, Platform platform, String videoId) {
    return scrapper.getDownloadLink(url, platform)
        .flatMap(downloadLink -> webClient
            .post()
            .uri(lambdaHost + "/process")
            .bodyValue(getRequestBody(downloadLink, videoId).toString())
            .retrieve()
            .bodyToMono(String.class));
  }

  private JSONObject getRequestBody(String url, String videoId) {
    JSONObject requestBody = new JSONObject();
    requestBody.put("url", url);
    requestBody.put("id", videoId);
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
