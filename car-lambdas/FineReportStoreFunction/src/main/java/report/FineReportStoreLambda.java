package report;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class FineReportStoreLambda implements RequestHandler<DynamodbEvent, Void> {

    private final String dbUrl = System.getenv("DB_URL");
    private final String dbUser = System.getenv("DB_USER");
    private final String dbPassword = System.getenv("DB_PASSWORD");

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            var newImage = record.getDynamodb().getNewImage();
            if (newImage == null) continue;

            String carPlate = newImage.get("carPlate").getS();
            String timestamp = newImage.get("validationTimestamp").getS();
            String email = newImage.get("email").getS();
            String price = newImage.get("price").getS();

            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                String sql = "INSERT INTO fine_reports (car_plate, email, validation_timestamp, price) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, carPlate);
                    stmt.setString(2, email);
                    stmt.setString(3, timestamp);
                    stmt.setString(4, price);
                    stmt.executeUpdate();
                }
            } catch (Exception e) {
                context.getLogger().log("ERROR: " + e.getMessage());
            }
        }
        return null;
    }
}
