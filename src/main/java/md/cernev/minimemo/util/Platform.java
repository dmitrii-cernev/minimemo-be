package md.cernev.minimemo.util;

public enum Platform {
  TIKTOK("tiktok"),
  INSTAGRAM("instagram"),
  SHORTS("shorts");

  private final String platform;

  Platform(String platform) {
    this.platform = platform;
  }

  public String getPlatform() {
    return platform;
  }
}
