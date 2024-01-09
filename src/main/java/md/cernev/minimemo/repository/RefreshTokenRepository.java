package md.cernev.minimemo.repository;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.entity.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Repository
public class RefreshTokenRepository {
    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    @Value("${aws.dynamodb.tableName}")
    private String tableName;

    public CompletableFuture<Optional<RefreshToken>> findByToken(String token) {
        QueryRequest queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .indexName("refreshToken-index")
            .keyConditionExpression("refreshToken = :refreshToken")
            .expressionAttributeValues(Map.of(":refreshToken", AttributeValue.builder().s(token).build()))
            .build();
        return dynamoDbAsyncClient.query(queryRequest).thenApply(queryResponse -> {
            if (queryResponse.hasItems() && queryResponse.items().size() == 1) {
                Map<String, AttributeValue> item = queryResponse.items().get(0);
                return Optional.of(RefreshToken.builder()
                    .id(item.get("id").s())
                    .type(item.get("type").s())
                    .userLogin(item.get("userId").s())
                    .refreshToken(item.get("refreshToken").s())
                    .expiration(item.get("expiration").s())
                    .build());
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<RefreshToken> save(RefreshToken refreshToken) {
        PutItemRequest putItemRequest = PutItemRequest.builder()
            .tableName(tableName)
            .item(Map.of(
                "id", AttributeValue.builder().s(refreshToken.getId()).build(),
                "type", AttributeValue.builder().s(refreshToken.getType()).build(),
                "userId", AttributeValue.builder().s(refreshToken.getUserLogin()).build(),
                "refreshToken", AttributeValue.builder().s(refreshToken.getRefreshToken()).build(),
                "expiration", AttributeValue.builder().s(refreshToken.getExpiration()).build()
            ))
            .build();
        return dynamoDbAsyncClient.putItem(putItemRequest).thenApply(putItemResponse -> refreshToken);
    }

    public void delete(RefreshToken token) {
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(Map.of("id", AttributeValue.builder().s(token.getId()).build()))
            .build();
        dynamoDbAsyncClient.deleteItem(deleteItemRequest).join();
    }
}
