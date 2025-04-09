package finesender;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

public class FineEmailSenderLambda implements RequestHandler<DynamodbEvent, Void> {

    private final AmazonSimpleEmailService sesClient = AmazonSimpleEmailServiceClientBuilder.defaultClient();

    private static final String SENDER_EMAIL = System.getenv("SENDER_EMAIL");

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            var newImage = record.getDynamodb().getNewImage();
            if (newImage == null) continue;

            // String recipientEmail = newImage.containsKey("email") ? newImage.get("email").getS() : "";
            String recipientEmail = "sauld70@yahoo.com";
            String carPlate = newImage.containsKey("carPlate") ? newImage.get("carPlate").getS() : "";
            String validationTimestamp = newImage.containsKey("validationTimestamp") ? newImage.get("validationTimestamp").getS() : "";            
            String price = newImage.containsKey("price") ? newImage.get("price").getS() : "";      

            String subject = "Parking Fine Notification";
            String body = String.format(
                "Dear driver,\n\nYou have received a fine.\n\nDetails:\nCar Plate: %s\nTime: %s\nAmount: $%s\n\nPlease resolve your payment promptly.\n",
                carPlate, validationTimestamp, price
            );

            try {
                sendEmail(recipientEmail, subject, body);
                context.getLogger().log("Email sent to: " + recipientEmail);
            } catch (Exception e) {
                context.getLogger().log("Failed to send email to: " + recipientEmail + "\nError: " + e.getMessage());
            }
        }
        return null;
    }

    private void sendEmail(String to, String subject, String body) {
        SendEmailRequest request = new SendEmailRequest()
            .withSource(SENDER_EMAIL)
            .withDestination(new Destination().withToAddresses(to))
            .withMessage(new Message()
                .withSubject(new Content().withCharset("UTF-8").withData(subject))
                .withBody(new Body().withText(new Content().withCharset("UTF-8").withData(body))));

        sesClient.sendEmail(request);
    }
}
