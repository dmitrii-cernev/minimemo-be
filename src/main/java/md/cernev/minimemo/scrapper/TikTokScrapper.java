package md.cernev.minimemo.scrapper;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TikTokScrapper {
  public static final String RAPID_API_LINK = "https://tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com/index?url=";
  public static final String RAPID_API_HOST = "tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com";
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TikTokScrapper.class);
  private final WebClient webClient;
  @Value("${rapid.api.key}")
  private String RAPID_API_KEY;


  public TikTokScrapper(WebClient webClient) {this.webClient = webClient;}

  public Mono<String> getDownloadLink(String url) {
    logger.info("Getting Tiktok video url...");
    return webClient
        .get()
        .uri(RAPID_API_LINK + url)
        .header("x-rapidapi-key", RAPID_API_KEY)
        .header("x-rapidapi-host", RAPID_API_HOST)
        .retrieve()
        .bodyToMono(String.class)
        .map(body -> new JSONObject(body).getJSONArray("video").getString(0));
  }
}
