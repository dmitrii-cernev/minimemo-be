package md.cernev.minimemo.controller;

import md.cernev.minimemo.service.PipelineService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin("http://localhost:5173")
public class PipelineController {
  private final PipelineService pipelineService;

  public PipelineController(PipelineService pipelineService) {this.pipelineService = pipelineService;}

  @PostMapping("/process")
  public Mono<String> startProcess(@RequestParam String url, @RequestParam String userId) {
    return pipelineService.startPipeline(url, userId);
  }
  @GetMapping("/video/{videoId}")
  public Mono<Object> getVideoInfo(@PathVariable String videoId) {
    return pipelineService.getVideoInfo(videoId);
  }

  @GetMapping("/videos/{userId}")
  public Mono<Object> getVideos(@PathVariable String userId) {
    return pipelineService.getVideos(userId);
  }

  @GetMapping("/ping")
  public Mono<String> ping() {
    return pipelineService.ping();
  }
}
