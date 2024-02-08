package md.cernev.minimemo.repository;

import md.cernev.minimemo.dto.MediaContentDto;
import md.cernev.minimemo.mapper.MediaContentMapper;
import md.cernev.minimemo.util.Platform;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class VideosRepository {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(VideosRepository.class);
  private final DynamoDbAsyncClient dynamoDbAsyncClient;
  @Value("${aws.dynamodb.tableName}")
  private String tableName;

  public VideosRepository(DynamoDbAsyncClient dynamoDbAsyncClient) {this.dynamoDbAsyncClient = dynamoDbAsyncClient;}

  public CompletableFuture<PutItemResponse> putItem(String userId, String videoId, String videoUrl, Platform platform) {
    Map<String, AttributeValue> itemValues = new HashMap<>();
    itemValues.put("userId", AttributeValue.builder().s(userId).build());
    itemValues.put("subId", AttributeValue.builder().s(videoId).build());
    itemValues.put("videoUrl", AttributeValue.builder().s(videoUrl).build());
    itemValues.put("platform", AttributeValue.builder().s(platform.name()).build());
    itemValues.put("status", AttributeValue.builder().s("PROCESSING").build());
    itemValues.put("createdAt", AttributeValue.builder().s(String.valueOf(System.currentTimeMillis())).build());
    PutItemRequest putItemRequest = PutItemRequest.builder()
        .item(itemValues)
        .tableName(tableName)
        .build();
    logger.info("Putting item in DynamoDB: {}", videoId);
    return dynamoDbAsyncClient.putItem(putItemRequest);
  }

  public CompletableFuture<UpdateItemResponse> updateItemStatus(String userId, String videoId, String status) {
    UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
        .tableName(tableName)
        .key(Map.of("userId", AttributeValue.builder().s(userId).build(), "subId", AttributeValue.builder().s(videoId)
            .build()))
        .attributeUpdates(Map.of("status", AttributeValueUpdate.builder().value(builder -> builder.s(status)).build()))
        .build();
    logger.info("Updating item in DynamoDB: {}", videoId);
    return dynamoDbAsyncClient.updateItem(updateItemRequest);
  }

  public CompletableFuture<MediaContentDto> getItem(String videoId) {
    QueryRequest queryRequest = QueryRequest.builder()
        .tableName(tableName)
        .indexName("subId-index")
        .keyConditionExpression("subId = :subId")
        .expressionAttributeValues(Map.of(":subId", AttributeValue.builder().s(videoId).build()))
        .build();
    logger.info("Getting item from DynamoDB: {}", videoId);
    return dynamoDbAsyncClient.query(queryRequest).thenApply(getItemResponse -> {
      if (getItemResponse.hasItems() && getItemResponse.items().size() == 1) {
        Map<String, AttributeValue> item = getItemResponse.items().get(0);
        return MediaContentMapper.map(item);
      }
      return new MediaContentDto();
    });
  }

  public CompletableFuture<List<MediaContentDto>> getItems(String userId)  {
    QueryRequest queryRequest = QueryRequest.builder()
        .tableName(tableName)
        .keyConditionExpression("userId = :userId")
        .expressionAttributeValues(Map.of(":userId", AttributeValue.builder().s(userId).build()))
        .build();
    logger.info("Getting items from DynamoDB: {}", userId);
    return dynamoDbAsyncClient.query(queryRequest).thenApply(queryResponse -> {
      if (queryResponse.hasItems()) {
        List<Map<String, AttributeValue>> items = queryResponse.items();
        return items.stream().filter(this::filterVideoItems).map(MediaContentMapper::map).toList();
      }
      return Collections.emptyList();
    });
  }

  public Mono<List<MediaContentDto>> findItems(String userId, String query) {
    Map<String, AttributeValue> expressionAttributeValues = Map.of(
        ":userId", AttributeValue.builder().s(userId).build(),
        ":value", AttributeValue.builder().s(query).build());
    QueryRequest queryRequest = QueryRequest.builder()
        .tableName(tableName)
        .keyConditionExpression("userId = :userId")
        .filterExpression("contains(title, :value) OR contains(tags, :value) OR contains(summary, :value) OR contains(transcription, :value)")
        .expressionAttributeValues(expressionAttributeValues)
        .build();
    logger.info("Finding items from DynamoDB: {}", query);
    return Mono.fromFuture(dynamoDbAsyncClient.query(queryRequest)).map(queryResponse -> {
      if (queryResponse.hasItems()) {
        List<Map<String, AttributeValue>> items = queryResponse.items();
        return items.stream().filter(this::filterVideoItems).map(MediaContentMapper::map).toList();
      }
      return Collections.emptyList();
    });
  }

  private boolean filterVideoItems(Map<String, AttributeValue> map) {
    return map.containsKey("platform") && map.containsKey("videoUrl");
  }
}
