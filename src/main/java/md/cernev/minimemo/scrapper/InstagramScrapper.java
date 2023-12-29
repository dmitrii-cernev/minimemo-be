package md.cernev.minimemo.scrapper;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InstagramScrapper {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(InstagramScrapper.class);
  private static final String RAPID_API_LINK = "https://instagram-downloader-download-instagram-videos-stories1.p.rapidapi.com/?url=";
  private static final String RAPID_API_HOST = "instagram-downloader-download-instagram-videos-stories1.p.rapidapi.com";
  private final WebClient webClient;
  @Value("${rapid.api.key}")
  private String RAPID_API_KEY;


  public InstagramScrapper(WebClient webClient) {this.webClient = webClient;}

  public Mono<String> getDownloadLink(String url) {
    logger.info("Getting Instagram video url...");
    return webClient
        .get()
        .uri(RAPID_API_LINK + url)
        .header("x-rapidapi-key", RAPID_API_KEY)
        .header("x-rapidapi-host", RAPID_API_HOST)
        .retrieve()
        .bodyToMono(String.class)
        .map(body -> new JSONArray(body).getJSONObject(0).getString("url"));
  }
}
