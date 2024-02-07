package md.cernev.minimemo.scrapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ShortsScrapper {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ShortsScrapper.class);
  private static final String RAPID_API_LINK = "https://youtube86.p.rapidapi.com/api/youtube/links";
  private static final String RAPID_API_HOST = "youtube86.p.rapidapi.com";
  private final WebClient webClient;
  @Value("${rapid.api.key}")
  private String RAPID_API_KEY;


  public ShortsScrapper(WebClient webClient) {this.webClient = webClient;}

  public Mono<String> getDownloadLink(String url) {
    logger.info("Getting Shorts video url...");
    JSONObject requestBody = new JSONObject();
    requestBody.put("url", url);
    return webClient
        .post()
        .uri(RAPID_API_LINK)
        .header("X-RapidAPI-Key", RAPID_API_KEY)
        .header("X-RapidAPI-Host", RAPID_API_HOST)
        .header("content-type", "application/json")
        .header("X-Forwarded-For", "70.41.3.18")
        .bodyValue(requestBody.toString())
        .retrieve()
        .bodyToMono(String.class)
        .map(body -> new JSONArray(body).getJSONObject(0).getJSONArray("urls").getJSONObject(0).getString("url"));

  }
}
