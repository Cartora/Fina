package fina.imitator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Random;

public class CarDataImitator {

    private static final String[] LOCATIONS = {
        "Herzl 10, Tel Aviv",
        "Rothschild 21, Tel Aviv",
        "Dizengoff 45, Tel Aviv"
    };

    private static final int UDP_PORT = 41234; // target port for UDP receiver
    private static final String UDP_HOST = "receiver.imitator-cluster"; // replace with your ECS or local address
    private static final int INTERVAL_MS = 30000; // send every 30 seconds


    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket();
        Random random = new Random();

        while (true) {
            String licensePlate = generateRandomPlate(random);
            String location = LOCATIONS[random.nextInt(LOCATIONS.length)];
            String timestamp = Instant.now().toString();

            String payload = String.format(
                "{\"licensePlate\":\"%s\",\"timestamp\":\"%s\",\"location\":\"%s\"}",
                licensePlate, timestamp, location
            );

            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(UDP_HOST), UDP_PORT);
            socket.send(packet);

            System.out.println("✅ Sent: " + payload);
            Thread.sleep(INTERVAL_MS);
        }
    }

    private static String generateRandomPlate(Random random) {
        int part1 = 10 + random.nextInt(90);      // 2 digits
        int part2 = 100 + random.nextInt(900);    // 3 digits
        int part3 = 10 + random.nextInt(90);      // 2 digits
        return part1 + "-" + part2 + "-" + part3;  // e.g. 25-438-79
    }
}
