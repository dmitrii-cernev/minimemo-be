package md.cernev.minimemo.repository;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.entity.Subscriptions;
import md.cernev.minimemo.entity.User;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(UserRepository.class);
    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    @Value("${aws.dynamodb.tableName}")
    private String tableName;

    public CompletableFuture<Optional<User>> findByLogin(String login) {
        QueryRequest queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .indexName("subId-index")
            .keyConditionExpression("subId = :subId")
            .expressionAttributeValues(Map.of(":subId", AttributeValue.builder().s(login).build()))
            .build();
        logger.info("Getting user from DynamoDB: {}", login);
        return dynamoDbAsyncClient.query(queryRequest).thenApply(queryResponse -> {
            if (queryResponse.hasItems() && queryResponse.items().size() == 1) {
                Map<String, AttributeValue> item = queryResponse.items().get(0);
                return Optional.of(User.builder()
                    .id(item.get("userId").s())
                    .firstName(item.get("firstName").s())
                    .lastName(item.get("lastName").s())
                    .login(item.get("subId").s())
                    .password(item.get("password").s())
                    .build());
            }
            return Optional.empty();
        });

    }

    public CompletableFuture<User> save(User user) {
        PutItemRequest putItemRequest = PutItemRequest.builder()
            .tableName(tableName)
            .item(Map.of(
                "userId", AttributeValue.builder().s(user.getId()).build(),
                "subId", AttributeValue.builder().s(user.getLogin()).build(),
                "firstName", AttributeValue.builder().s(user.getFirstName()).build(),
                "lastName", AttributeValue.builder().s(user.getLastName()).build(),
                "subscription", AttributeValue.builder().s(Subscriptions.FREE.getSubscription()).build(),
                "password", AttributeValue.builder().s(user.getPassword()).build()
            ))
            .build();
        logger.info("Putting user in DynamoDB: {}", user.getLogin());
        return dynamoDbAsyncClient.putItem(putItemRequest).thenApply(putItemResponse -> user);
    }
}
