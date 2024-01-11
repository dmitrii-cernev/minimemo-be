package md.cernev.minimemo.repository;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.entity.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Repository
public class RefreshTokenRepository {
    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    @Value("${aws.dynamodb.tableName}")
    private String tableName;

    public CompletableFuture<Optional<RefreshToken>> findByToken(String userId, String token) {
        GetItemRequest getItemRequest = GetItemRequest.builder()
            .tableName(tableName)
            .key(Map.of(
                "userId", AttributeValue.builder().s(userId).build(),
                "subId", AttributeValue.builder().s(token).build()
            ))
            .build();
        return dynamoDbAsyncClient.getItem(getItemRequest).thenApply(response -> {
            if (response.hasItem()) {
                Map<String, AttributeValue> item = response.item();
                return Optional.of(RefreshToken.builder()
                    .userId(item.get("userId").s())
                    .refreshToken(item.get("subId").s())
                    .userLogin(item.get("login").s())
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
                "userId", AttributeValue.builder().s(refreshToken.getUserId()).build(),
                "subId", AttributeValue.builder().s(refreshToken.getRefreshToken()).build(),
                "login", AttributeValue.builder().s(refreshToken.getUserLogin()).build(),
                "expiration", AttributeValue.builder().s(refreshToken.getExpiration()).build()
            ))
            .build();
        return dynamoDbAsyncClient.putItem(putItemRequest).thenApply(putItemResponse -> refreshToken);
    }

    public void delete(RefreshToken token) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("userId", AttributeValue.builder().s(token.getUserId()).build());
        key.put("subId", AttributeValue.builder().s(token.getRefreshToken()).build());
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();
        dynamoDbAsyncClient.deleteItem(deleteItemRequest).join();
    }
}
