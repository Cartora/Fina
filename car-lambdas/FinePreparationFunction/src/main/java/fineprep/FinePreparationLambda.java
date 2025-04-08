package fineprep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

public class FinePreparationLambda implements RequestHandler<DynamodbEvent, Void> {

    private static final String PRICE = System.getenv("PRICE");
    private static final String FINES_TABLE = System.getenv("FINES_TABLE");
    private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            var newImage = record.getDynamodb().getNewImage();
            if (newImage == null) continue;

            String carPlate = newImage.get("carPlate").getS();
            String validationTimestamp = newImage.get("validationTimestamp").getS();
            String email = carPlate + "@fina.com";

            context.getLogger().log(String.format(
                "Preparing fine for carPlate=%s at time=%s with email=%s and price=%s\n",
                carPlate, validationTimestamp, email, PRICE
            ));

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("carPlate", new AttributeValue(carPlate));
            item.put("validationTimestamp", new AttributeValue(validationTimestamp));
            item.put("email", new AttributeValue(email));
            item.put("price", new AttributeValue(PRICE));

            PutItemRequest request = new PutItemRequest()
                .withTableName(FINES_TABLE)
                .withItem(item);

            dynamoDB.putItem(request);
        }
        return null;
    }
}
