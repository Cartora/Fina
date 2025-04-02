package store;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.*;

public class StoreCarData implements RequestHandler<Object, String> {

    private static final String[] LOCATIONS = {
            "Herzl 10, Tel Aviv",
            "Rothschild 21, Tel Aviv",
            "Dizengoff 45, Tel Aviv"
    };

    private final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(software.amazon.awssdk.regions.Region.of("us-east-1"))
            .build();

    private final String tableName = System.getenv("TABLE_NAME");

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("🚀 Lambda execution started.\n");

        Random random = new Random();
        String licensePlate = generateRandomPlate(random);
        String location = LOCATIONS[random.nextInt(LOCATIONS.length)];
        String timestamp = Instant.now().toString();

        context.getLogger().log("📦 Generated data:\n");
        context.getLogger().log("  License Plate: " + licensePlate + "\n");
        context.getLogger().log("  Location: " + location + "\n");
        context.getLogger().log("  Timestamp: " + timestamp + "\n");
        context.getLogger().log("  DynamoDB Table: " + tableName + "\n");

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("licensePlate", AttributeValue.builder().s(licensePlate).build());
        item.put("timestamp", AttributeValue.builder().s(timestamp).build());
        item.put("location", AttributeValue.builder().s(location).build());

        try {
            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            context.getLogger().log("📨 Sending item to DynamoDB...\n");
            dynamoDb.putItem(request);
            context.getLogger().log("✅ Successfully stored item in DynamoDB.\n");

            return "✅ Inserted: " + licensePlate + " @ " + timestamp;
        } catch (Exception e) {
            context.getLogger().log("❌ Error storing item: " + e.getMessage() + "\n");
            e.printStackTrace();
            return "❌ Failed: " + e.getMessage();
        }
    }

    private static String generateRandomPlate(Random random) {
        int part1 = 10 + random.nextInt(90);
        int part2 = 100 + random.nextInt(900);
        int part3 = 10 + random.nextInt(90);
        return part1 + "-" + part2 + "-" + part3;
    }
}
