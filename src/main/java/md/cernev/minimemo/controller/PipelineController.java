package md.cernev.minimemo.controller;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.configuration.security.UserAuthProvider;
import md.cernev.minimemo.service.PipelineService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PipelineController {
  private final PipelineService pipelineService;
  private final UserAuthProvider userAuthProvider;

  @PostMapping("/process")
  public Mono<String> startProcess(@RequestHeader(name = "Authorization") String token, @RequestParam String url) {
    String userId = userAuthProvider.getUserId(token);
    return pipelineService.startPipeline(url, userId);
  }
  @GetMapping("/video/{videoId}")
  public Mono<Object> getVideoInfo(@PathVariable String videoId) {
    return pipelineService.getVideoInfo(videoId);
  }

  @GetMapping("/videos")
  public Mono<Object> getVideos(@RequestHeader(name = "Authorization") String token) {
    String userId = userAuthProvider.getUserId(token);
    return pipelineService.getVideos(userId);
  }

}
