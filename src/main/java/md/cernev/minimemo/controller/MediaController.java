package md.cernev.minimemo.controller;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.configuration.security.UserAuthProvider;
import md.cernev.minimemo.dto.MediaContentDto;
import md.cernev.minimemo.service.MediaService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MediaController {
  private final MediaService mediaService;
  private final UserAuthProvider userAuthProvider;

  @PostMapping("/process")
  public Mono<String> startProcess(@RequestHeader(name = "Authorization") String token, @RequestParam String url) {
    String userId = userAuthProvider.getUserId(token);
    return mediaService.startPipeline(url, userId);
  }
  @GetMapping("/video/{videoId}")
  public Mono<Object> getVideoInfo(@PathVariable String videoId) {
    return mediaService.getVideoInfo(videoId);
  }

  @GetMapping("/videos")
  public Mono<Object> getVideos(@RequestHeader(name = "Authorization") String token) {
    String userId = userAuthProvider.getUserId(token);
    return mediaService.getVideos(userId);
  }

  @GetMapping("/video/search")
  public Mono<List<MediaContentDto>> searchVideos(@RequestHeader(name = "Authorization") String token, @RequestParam(required = false, defaultValue = "") String query) {
    String userId = userAuthProvider.getUserId(token);
    return mediaService.findVideos(userId, query);
  }

}
