package fina.receiver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class CarDataReceiver {

    private static final int UDP_PORT = 41234;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
            System.out.println("🚦 UDP Receiver started on port " + UDP_PORT);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.printf("📨 Received from %s:%d → %s%n",
                        packet.getAddress().getHostAddress(),
                        packet.getPort(),
                        message);
            }
        } catch (Exception e) {
            System.err.println("❌ Error in UDP receiver: " + e.getMessage());
            e.printStackTrace();
        }
    }
}