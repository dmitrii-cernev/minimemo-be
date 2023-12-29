package md.cernev.minimemo.repository;

import md.cernev.minimemo.dto.MediaContentDto;
import md.cernev.minimemo.mapper.MediaContentMapper;
import md.cernev.minimemo.util.Platform;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class MiniMemoRepository {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MiniMemoRepository.class);
  private final DynamoDbAsyncClient dynamoDbAsyncClient;
  @Value("${aws.dynamodb.tableName}")
  private String tableName;

  public MiniMemoRepository(DynamoDbAsyncClient dynamoDbAsyncClient) {this.dynamoDbAsyncClient = dynamoDbAsyncClient;}

  public CompletableFuture<PutItemResponse> putItem(String userId, String videoId, String videoUrl, Platform platform) {
    Map<String, AttributeValue> itemValues = new HashMap<>();
    itemValues.put("id", AttributeValue.builder().s(videoId).build());
    itemValues.put("userId", AttributeValue.builder().s(userId).build());
    itemValues.put("videoUrl", AttributeValue.builder().s(videoUrl).build());
    itemValues.put("platform", AttributeValue.builder().s(platform.name()).build());
    itemValues.put("createdAt", AttributeValue.builder().s(String.valueOf(System.currentTimeMillis())).build());
    PutItemRequest putItemRequest = PutItemRequest.builder()
        .item(itemValues)
        .tableName(tableName)
        .build();
    logger.info("Putting item in DynamoDB: {}", videoId);
    return dynamoDbAsyncClient.putItem(putItemRequest);
  }

  public CompletableFuture<MediaContentDto> getItem(String videoId) {
    GetItemRequest getItemRequest = GetItemRequest.builder()
        .tableName(tableName)
        .key(Map.of("id", AttributeValue.builder().s(videoId).build()))
        .build();
    logger.info("Getting item from DynamoDB: {}", videoId);
    return dynamoDbAsyncClient.getItem(getItemRequest).thenApply(getItemResponse -> {
      if (getItemResponse.hasItem()) {
        Map<String, AttributeValue> item = getItemResponse.item();
        return MediaContentMapper.map(item);
      }
      return new MediaContentDto();
    });
  }

  public CompletableFuture<List<MediaContentDto>> getItems(String userId)  {
    QueryRequest queryRequest = QueryRequest.builder()
        .tableName(tableName)
        .indexName("userId-index")
        .keyConditionExpression("userId = :userId")
        .expressionAttributeValues(Map.of(":userId", AttributeValue.builder().s(userId).build()))
        .build();
    logger.info("Getting items from DynamoDB: {}", userId);
    return dynamoDbAsyncClient.query(queryRequest).thenApply(queryResponse -> {
      if (queryResponse.hasItems()) {
        List<Map<String, AttributeValue>> items = queryResponse.items();
        return items.stream().map(MediaContentMapper::map).toList();
      }
      return Collections.emptyList();
    });
  }
}
