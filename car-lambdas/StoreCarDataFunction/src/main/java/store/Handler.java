package store;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, String> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.builder()
    .region(software.amazon.awssdk.regions.Region.of("eu-north-1"))
    .build();
    private final String tableName = System.getenv("TABLE_NAME");

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        try {
            String licensePlate = (String) input.get("licensePlate");
            String location = (String) input.get("location");
            String timestamp = Instant.now().toString();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("licensePlate", AttributeValue.builder().s(licensePlate).build());
            item.put("timestamp", AttributeValue.builder().s(timestamp).build());
            item.put("location", AttributeValue.builder().s(location).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDb.putItem(request);

            return "✅ Stored item: " + licensePlate;
        } catch (Exception e) {
            return "❌ Failed to store item: " + e.getMessage();
        }
    }
}
