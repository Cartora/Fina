package payment;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PaymentCheckerLambda implements RequestHandler<DynamodbEvent, Void> {

    private static final String BUCKET_NAME = System.getenv("VALIDATION_BUCKET");
    private static final String UNPAID_DYNAMODB_TABLE = System.getenv("UNPAID_TABLE");
    private static final long TOLERANCE_TIME_SECONDS = Long.valueOf(System.getenv("TOLERANCE_TIME_SECONDS"));
    private static final long COOLDOWN_TIME_SECONDS = Long.valueOf(System.getenv("COOLDOWN_TIME_SECONDS"));

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
    private final Random random = new Random();

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            var newImage = record.getDynamodb().getNewImage();

            if (newImage == null || !newImage.containsKey("carPlate") || newImage.get("carPlate").getS().isEmpty()) {
                continue;
            }

            String carPlate = newImage.get("carPlate").getS();
            String timestampStr = newImage.get("timestamp").getS();
            String slotId = newImage.get("slotId").getS();
            Instant timestamp = Instant.parse(timestampStr);
            Instant now = Instant.now();

            context.getLogger()
                    .log("Car " + carPlate + " timestampStr: " + timestampStr + " slotId: " + slotId + " now: " + now);

            try {
                if (s3Client.doesObjectExist(BUCKET_NAME, carPlate)) {
                    // Read current nextValidationTime from S3
                    context.getLogger().log("Found " + carPlate + " in S3 bucket. Checking validation time.");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            s3Client.getObject(BUCKET_NAME, carPlate).getObjectContent()));
                    Instant nextValidationTime = Instant.parse(reader.readLine());
                    context.getLogger().log("Next validation time for " + carPlate + ": " + nextValidationTime);
                    
                    if (nextValidationTime.isBefore(now)) {
                        boolean isPaid = random.nextInt(100) < 85;
                        context.getLogger().log("Car " + carPlate + " validated: " + (isPaid ? "Paid" : "Not Paid"));

                        if (!isPaid) {
                            // Send to unpaid DynamoDB table
                            Map<String, AttributeValue> item = new HashMap<>();
                            item.put("carPlate", new AttributeValue(carPlate));
                            item.put("slotId", new AttributeValue(slotId));
                            item.put("timestamp", new AttributeValue(timestampStr));

                            PutItemRequest request = new PutItemRequest()
                                    .withTableName(UNPAID_DYNAMODB_TABLE)
                                    .withItem(item);

                            dynamoDBClient.putItem(request);
                            context.getLogger().log("Sent unpaid car " + carPlate + " to DynamoDB.");
                        }

                        // Update next validation time
                        Instant newValidation = now.plusSeconds(COOLDOWN_TIME_SECONDS);
                        s3Client.putObject(BUCKET_NAME, carPlate, newValidation.toString());
                        context.getLogger().log("Updated " + carPlate + " validation time to: " + newValidation);
                    } else {
                        context.getLogger().log("Car " + carPlate + " is still within validation time. Skipping.");
                    }

                } else {
                    // Insert for first time
                    Instant nextValidationTime = timestamp.plusSeconds(TOLERANCE_TIME_SECONDS);
                    s3Client.putObject(BUCKET_NAME, carPlate, nextValidationTime.toString());
                    context.getLogger()
                            .log("Added new carPlate " + carPlate + " with validation: " + nextValidationTime);
                }

            } catch (Exception e) {
                context.getLogger().log("Error with " + carPlate + ": " + e.getMessage());
            }
        }

        return null;
    }
}