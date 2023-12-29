package md.cernev.minimemo.mapper;

import md.cernev.minimemo.dto.MediaContentDto;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

@Component
public class MediaContentMapper {

  private MediaContentMapper() {
  }

  public static MediaContentDto map(Map<String, AttributeValue> item) {
    MediaContentDto mediaContentDto = new MediaContentDto();
    mediaContentDto.setId(item.get("id").s());
    AttributeValue defaultValue = AttributeValue.builder().build();
    mediaContentDto.setVideoUrl(item.getOrDefault("videoUrl", defaultValue).s());
    mediaContentDto.setTitle(item.getOrDefault("title", defaultValue).s());
    mediaContentDto.setSummary(item.getOrDefault("summary", defaultValue).s());
    mediaContentDto.setTranscription(item.getOrDefault("transcription", defaultValue).s());
    mediaContentDto.setPlatform(item.getOrDefault("platform", defaultValue).s());
    mediaContentDto.setCreatedAt(item.getOrDefault("createdAt", defaultValue).s());
    return mediaContentDto;
  }
}
