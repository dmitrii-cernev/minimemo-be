package md.cernev.minimemo.controller;

import md.cernev.minimemo.service.MediaService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller for endpoints without auth
 */
@RestController()
@RequestMapping("/api/open")
public class OpenPipelineController {
    private final MediaService mediaService;

    public OpenPipelineController(MediaService mediaService) {this.mediaService = mediaService;}

    @PostMapping("/process")
    public Mono<String> startProcess(@RequestParam String url, @RequestParam String userId) {
        return mediaService.startPipeline(url, userId);
    }

    @GetMapping("/video/{videoId}")
    public Mono<Object> getVideoInfo(@PathVariable String videoId) {
        return mediaService.getVideoInfo(videoId);
    }

    @GetMapping("/videos/{userId}")
    public Mono<Object> getVideos(@PathVariable String userId) {
        return mediaService.getVideos(userId);
    }

    @GetMapping("/ping")
    public Mono<String> ping() {
        return mediaService.ping();
    }
}
