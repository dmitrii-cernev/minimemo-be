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
    public static final String USER_ID = "userId";
    public static final String SUB_ID = "subId";
    public static final String REFRESH_TOKEN = "refreshToken";
    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    @Value("${aws.dynamodb.tableName}")
    private String tableName;

    public CompletableFuture<Optional<RefreshToken>> findByToken(String userId, String token) {
        GetItemRequest getItemRequest = GetItemRequest.builder()
            .tableName(tableName)
            .key(Map.of(
                USER_ID, AttributeValue.builder().s(userId).build(),
                SUB_ID, AttributeValue.builder().s(REFRESH_TOKEN).build()
            ))
            .build();
        return dynamoDbAsyncClient.getItem(getItemRequest).thenApply(response -> {
            if (response.hasItem()) {
                Map<String, AttributeValue> item = response.item();
                if (item.get(REFRESH_TOKEN).s().equals(token)) {
                    return Optional.of(RefreshToken.builder()
                        .userId(item.get(USER_ID).s())
                        .refreshToken(item.get(SUB_ID).s())
                        .userLogin(item.get("login").s())
                        .expiration(item.get("expiration").s())
                        .build());
                }
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<RefreshToken> save(RefreshToken refreshToken) {
        PutItemRequest putItemRequest = PutItemRequest.builder()
            .tableName(tableName)
            .item(Map.of(
                USER_ID, AttributeValue.builder().s(refreshToken.getUserId()).build(),
                SUB_ID, AttributeValue.builder().s(REFRESH_TOKEN).build(),
                "login", AttributeValue.builder().s(refreshToken.getUserLogin()).build(),
                REFRESH_TOKEN, AttributeValue.builder().s(refreshToken.getRefreshToken()).build(),
                "expiration", AttributeValue.builder().s(refreshToken.getExpiration()).build()
            ))
            .build();
        return dynamoDbAsyncClient.putItem(putItemRequest).thenApply(putItemResponse -> refreshToken);
    }

    public void delete(RefreshToken token) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(USER_ID, AttributeValue.builder().s(token.getUserId()).build());
        key.put(SUB_ID, AttributeValue.builder().s(REFRESH_TOKEN).build());
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();
        dynamoDbAsyncClient.deleteItem(deleteItemRequest).join();
    }
}
