package md.cernev.minimemo.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.cernev.minimemo.dto.CountDto;
import md.cernev.minimemo.util.CustomHttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SubscriptionRepository {
    public static final String USER_ID = "userId";
    public static final String SUB_ID = "subId";
    public static final String SUBSCRIPTION = "subscription";
    public static final String COUNT = "count";
    public static final String TOTAL_COUNT = "totalCount";
    private static final String CREATED_AT = "createdAt";
    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    @Value("${aws.dynamodb.tableName}")
    private String tableName;

    public Mono<CountDto> getOrCreateSubscriptionsCount(String userId) {
        GetItemRequest getItemRequest = GetItemRequest.builder()
            .tableName(tableName)
            .key(Map.of(
                USER_ID, AttributeValue.builder().s(userId).build(),
                SUB_ID, AttributeValue.builder().s(SUBSCRIPTION).build()
            ))
            .build();
        return Mono.fromFuture(dynamoDbAsyncClient.getItem(getItemRequest)).flatMap(response -> {
            if (response.hasItem()) {
                Map<String, AttributeValue> item = response.item();
                return Mono.just(CountDto.builder()
                    .count(Integer.parseInt(item.get(COUNT).n()))
                    .totalCount(Integer.parseInt(item.get(TOTAL_COUNT).n()))
                    .build());
            }
            return createSubscriptionsCount(userId);
        });
    }

    public Mono<CountDto> createSubscriptionsCount(String userId) {
        return Mono.fromFuture(dynamoDbAsyncClient.putItem(builder -> builder
            .tableName(tableName)
            .item(Map.of(
                USER_ID, AttributeValue.builder().s(userId).build(),
                SUB_ID, AttributeValue.builder().s(SUBSCRIPTION).build(),
                COUNT, AttributeValue.builder().n("3").build(),
                TOTAL_COUNT, AttributeValue.builder().n("3").build(),
                CREATED_AT, AttributeValue.builder().n(String.valueOf(System.currentTimeMillis())).build()
            ))
        )).map(response -> CountDto.builder()
            .count(3)
            .totalCount(3)
            .build());
    }

    public Mono<CountDto> decrementCount(String userId) {
        return Mono.fromFuture(dynamoDbAsyncClient.updateItem(builder -> builder
            .tableName(tableName)
            .key(Map.of(
                USER_ID, AttributeValue.builder().s(userId).build(),
                SUB_ID, AttributeValue.builder().s(SUBSCRIPTION).build()
            ))
            .updateExpression("SET #count = #count - :val")
            .conditionExpression("#count > :min")
            .expressionAttributeNames(Map.of(
                "#count", COUNT
            ))
            .expressionAttributeValues(Map.of(
                ":val", AttributeValue.builder().n("1").build(),
                ":min", AttributeValue.builder().n("0").build()
            ))
            .returnValues("ALL_NEW")
        )).map(response -> {
            Map<String, AttributeValue> attributes = response.attributes();
            return CountDto.builder()
                .count(Integer.parseInt(attributes.get(COUNT).n()))
                .totalCount(Integer.parseInt(attributes.get(TOTAL_COUNT).n()))
                .build();
        }).onErrorMap(error -> {
            if (error instanceof software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException) {
                return new CustomHttpException("Count is 0", HttpStatus.BAD_REQUEST);
            } else {
                log.error("Error decrementing count", error);
                return new CustomHttpException("Error decrementing count", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });

    }

    public Mono<CountDto> incrementCount(String userId) {
        return Mono.fromFuture(dynamoDbAsyncClient.updateItem(builder -> builder
            .tableName(tableName)
            .key(Map.of(
                USER_ID, AttributeValue.builder().s(userId).build(),
                SUB_ID, AttributeValue.builder().s(SUBSCRIPTION).build()
            ))
            .updateExpression("SET #count = #count + :val")
            .conditionExpression("#count <= " + TOTAL_COUNT)
            .expressionAttributeNames(Map.of(
                "#count", COUNT
            ))
            .expressionAttributeValues(Map.of(
                ":val", AttributeValue.builder().n("1").build()
            ))
            .returnValues("ALL_NEW")
        )).map(response -> {
            Map<String, AttributeValue> attributes = response.attributes();
            return CountDto.builder()
                .count(Integer.parseInt(attributes.get(COUNT).n()))
                .totalCount(Integer.parseInt(attributes.get(TOTAL_COUNT).n()))
                .build();
        }).onErrorMap(error -> {
            if (error instanceof software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException) {
                return new CustomHttpException("Exceeded total count", HttpStatus.BAD_REQUEST);
            } else {
                log.error("Error incrementing count", error);
                return new CustomHttpException("Error incrementing count", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }
}
