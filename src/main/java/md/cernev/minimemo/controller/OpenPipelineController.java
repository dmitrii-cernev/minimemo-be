package md.cernev.minimemo.controller;

import md.cernev.minimemo.service.PipelineService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller for endpoints without auth
 */
@RestController()
@RequestMapping("/api/open")
public class OpenPipelineController {
    private final PipelineService pipelineService;

    public OpenPipelineController(PipelineService pipelineService) {this.pipelineService = pipelineService;}

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
