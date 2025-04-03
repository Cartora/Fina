package store;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StoreCarData implements RequestHandler<Object, String> {

    private static final int TOTAL_SLOTS = 10;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(software.amazon.awssdk.regions.Region.of("us-east-1"))
            .build();

    private final String tableName = System.getenv("TABLE_NAME");
    private final Random random = new Random();

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("\n🚦 Parking camera simulation started\n");

        Instant now = Instant.now();
        String timestamp = TIMESTAMP_FORMATTER.format(now);
        int hour = now.atZone(ZoneOffset.UTC).getHour();

        for (long slotId = 1; slotId <= TOTAL_SLOTS; slotId++) {
            String previousPlate = fetchLatestPlateForSlot(slotId);
            String plate = previousPlate;

            if (plate != null && !plate.isEmpty() && shouldLeaveCar(hour)) {
                plate = null;
            }

            if ((plate == null || plate.isEmpty()) && shouldParkNewCar(hour)) {
                plate = generateCarPlate();
            }

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("slotId", AttributeValue.builder().n(String.valueOf(slotId)).build());
            item.put("timestamp", AttributeValue.builder().s(timestamp).build());
            item.put("carPlate", AttributeValue.builder().s(plate == null ? "" : plate).build());

            try {
                PutItemRequest request = PutItemRequest.builder()
                        .tableName(tableName)
                        .item(item)
                        .build();

                dynamoDb.putItem(request);
                context.getLogger().log("✅ Saved slot " + slotId + " with plate: " + (plate == null ? "EMPTY" : plate) + "\n");
            } catch (Exception e) {
                context.getLogger().log("❌ Error writing slot " + slotId + ": " + e.getMessage() + "\n");
            }
        }

        return "✅ Parking snapshot saved at: " + timestamp;
    }

    private String fetchLatestPlateForSlot(long slotId) {
        try {
            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(tableName)
                    .keyConditionExpression("slotId = :slot")
                    .expressionAttributeValues(Map.of(
                            ":slot", AttributeValue.builder().n(String.valueOf(slotId)).build()
                    ))
                    .scanIndexForward(false) // latest first
                    .limit(1)
                    .build();

            QueryResponse response = dynamoDb.query(queryRequest);
            if (!response.items().isEmpty()) {
                String plate = response.items().get(0).get("carPlate").s();
                return plate.isEmpty() ? null : plate;
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to fetch latest plate for slot " + slotId + ": " + e.getMessage());
        }
        return null;
    }

    private boolean shouldParkNewCar(int hour) {
        if (hour < 6) return random.nextDouble() < 0.1;
        if (hour < 9) return random.nextDouble() < 0.4;
        if (hour < 18) return random.nextDouble() < 0.7;
        if (hour < 22) return random.nextDouble() < 0.5;
        return random.nextDouble() < 0.2;
    }

    private boolean shouldLeaveCar(int hour) {
        if (hour < 6) return random.nextDouble() < 0.05;
        if (hour < 9) return random.nextDouble() < 0.03;
        if (hour < 18) return random.nextDouble() < 0.01;
        if (hour < 22) return random.nextDouble() < 0.03;
        return random.nextDouble() < 0.05;
    }

    private String generateCarPlate() {
        return String.format("%02d-%03d-%02d",
                random.nextInt(100),
                random.nextInt(1000),
                random.nextInt(100));
    }
}
