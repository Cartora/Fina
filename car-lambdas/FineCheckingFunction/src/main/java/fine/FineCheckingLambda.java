package fine;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FineCheckingLambda implements RequestHandler<DynamodbEvent, Void> {

    private static final double FINE_PROBABILITY = 0.15;
    private static final String TABLE_NAME = System.getenv("FINE_CANDIDATES_TABLE");

    private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
    private final Random random = new Random();

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            var newImage = record.getDynamodb().getNewImage();
            if (newImage == null)
                continue;

            String carPlate = newImage.get("carPlate").getS();
            String slotId = newImage.get("slotId").getS();
            String timestamp = newImage.get("timestamp").getS();

            boolean hasFine = random.nextDouble() < FINE_PROBABILITY;

            if (hasFine) {
                context.getLogger().log("Car " + carPlate + " already has a fine.\n");
            } else {
                context.getLogger()
                        .log("Car " + carPlate + " does not have a fine. Adding to FineCandidates table...\n");

                Map<String, AttributeValue> item = new HashMap<>();
                item.put("slotId", new AttributeValue(slotId));
                item.put("carPlate", new AttributeValue(carPlate));
                item.put("validationTimestamp", new AttributeValue(timestamp));

                PutItemRequest request = new PutItemRequest()
                        .withTableName(TABLE_NAME)
                        .withItem(item);

                dynamoDB.putItem(request);
            }
        }
        return null;
    }
}
