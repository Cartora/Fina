package fine;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.HashMap;
import java.util.Map;

public class FineCheckingLambda implements RequestHandler<DynamodbEvent, Void> {

    private static final String FINE_CANDIDATES_TABLE = System.getenv("FINE_CANDIDATES_TABLE");

    private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            var newImage = record.getDynamodb().getNewImage();
            if (newImage == null) continue;

            String carPlate = newImage.get("carPlate").getS();
            String slotId = newImage.get("slotId").getS();
            String timestamp = newImage.get("timestamp").getS();

            if (isAlreadyCandidate(slotId, carPlate)) {
                context.getLogger().log("Car " + carPlate + " is already in FineCandidates. Skipping...\n");
                continue;
            }

            context.getLogger().log("Car " + carPlate + " is not yet in FineCandidates. Inserting...\n");

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("slotId", new AttributeValue(slotId));
            item.put("carPlate", new AttributeValue(carPlate));
            item.put("validationTimestamp", new AttributeValue(timestamp));

            PutItemRequest request = new PutItemRequest()
                    .withTableName(FINE_CANDIDATES_TABLE)
                    .withItem(item);

            dynamoDB.putItem(request);
        }

        return null;
    }

    private boolean isAlreadyCandidate(String slotId, String carPlate) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("slotId", new AttributeValue(slotId));
        key.put("carPlate", new AttributeValue(carPlate));

        GetItemRequest request = new GetItemRequest()
                .withTableName(FINE_CANDIDATES_TABLE)
                .withKey(key);

        Map<String, AttributeValue> item = dynamoDB.getItem(request).getItem();
        return item != null;
    }
}