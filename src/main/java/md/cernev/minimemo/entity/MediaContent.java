package md.cernev.minimemo.entity;

import lombok.Data;

@Data
public class MediaContent {
  private String id;
  private String userId;
  private String videoUrl;
  private String title;
  private String summary;
  private String transcription;
  private String tags;
  private String platform;
  private String createdAt;
  private String updatedAt;
}
