package md.cernev.minimemo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Configuration
public class AwsConfiguration {
  @Value("${aws.credentials.accessKey}")
  public String accessKey;
  @Value("${aws.credentials.secretKey}")
  public String secretKey;
  @Value("${aws.region}")
  public String region;

  @Bean
  public DynamoDbAsyncClient getDynamoDbClient() {
    return DynamoDbAsyncClient.builder()
        .credentialsProvider(this::getCredentials)
        .region(getRegion())
        .build();
  }

  @Bean
  public AwsBasicCredentials getCredentials() {
    return AwsBasicCredentials.create(accessKey, secretKey);
  }

  @Bean
  public Region getRegion() {
    return Region.of(region);
  }

}
