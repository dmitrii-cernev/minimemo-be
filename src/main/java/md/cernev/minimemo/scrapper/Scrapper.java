package md.cernev.minimemo.scrapper;

import md.cernev.minimemo.util.Platform;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Scrapper {
  private final InstagramScrapper instagramScrapper;
  private final TikTokScrapper tikTokScrapper;
  private final ShortsScrapper shortsScrapper;

  public Scrapper(InstagramScrapper instagramScrapper, TikTokScrapper tikTokScrapper, ShortsScrapper shortsScrapper) {
    this.instagramScrapper = instagramScrapper;
    this.tikTokScrapper = tikTokScrapper;
    this.shortsScrapper = shortsScrapper;
  }

  public Mono<String> getDownloadLink(String url, Platform platform) {
    return switch (platform) {
      case TIKTOK -> tikTokScrapper.getDownloadLink(url);
      case INSTAGRAM -> instagramScrapper.getDownloadLink(url);
      case SHORTS -> shortsScrapper.getDownloadLink(url);
    };
  }
}
