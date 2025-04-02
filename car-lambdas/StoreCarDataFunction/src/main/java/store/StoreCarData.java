package store;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class StoreCarData implements RequestHandler<Object, String> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(software.amazon.awssdk.regions.Region.of("us-east-1"))
            .build();

    private final String tableName = System.getenv("TABLE_NAME");

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log(" Lambda execution started.\n");

        CarData[] cars = new CarData[] {
                new CarData("85-382-13", "Herzl 10, Tel Aviv", 0, 60),

                new CarData("59-545-79", "Rothschild 21, Tel Aviv", 0, 5),

                new CarData("59-545-79", "Rothschild 21, Tel Aviv", 30, 30)
        };

        for (CarData car : cars) {
            String enteringTime = car.getEnteringTime().toString();
            String lastSeenTime = car.getLastSeenTime().toString();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("licensePlate", AttributeValue.builder().s(car.licensePlate).build());
            item.put("entryTime", AttributeValue.builder().s(enteringTime).build());
            item.put("lastSeen", AttributeValue.builder().s(lastSeenTime).build());
            item.put("location", AttributeValue.builder().s(car.location).build());

            try {
                PutItemRequest request = PutItemRequest.builder()
                        .tableName(tableName)
                        .item(item)
                        .build();

                context.getLogger().log("📨 Inserting data for car: " + car.licensePlate + "\n");
                dynamoDb.putItem(request);
                context.getLogger().log("✅ Stored in DynamoDB: " + car.licensePlate + "\n");
            } catch (Exception e) {
                context.getLogger().log("❌ Error storing car " + car.licensePlate + ": " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }

        return "✅ Inserted static car data";
    }

    static class CarData {
        String licensePlate;
        String location;
        Instant enteringTime;
        Instant lastSeenTime;

        CarData(String licensePlate, String location, long enteringDelayMin, long durationMinutes) {
            this.licensePlate = licensePlate;
            this.location = location;
        
            // Set enteringTime to 10:00 AM on the current day
            Instant tenAM = Instant.now()
                    .truncatedTo(ChronoUnit.DAYS) // Start of the day
                    .plus(10, ChronoUnit.HOURS); // Add 10 hours to get 10:00 AM
        
            this.enteringTime = tenAM.plus(enteringDelayMin, ChronoUnit.MINUTES);
            this.lastSeenTime = this.enteringTime.plus(durationMinutes, ChronoUnit.MINUTES);
        }

        public Instant getEnteringTime() {
            return enteringTime;
        }

        public Instant getLastSeenTime() {
            return lastSeenTime;
        }
    }
}
