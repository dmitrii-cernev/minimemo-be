package md.cernev.minimemo.dto;

import lombok.Data;

@Data
public class MediaContentDto {
    private String userId;
    private String subId;
  private String videoUrl;
  private String title;
  private String summary;
  private String transcription;
  private String platform;
  private String createdAt;
}
