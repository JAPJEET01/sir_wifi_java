import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceSenderReceiver {
    public static final int DEFAULT_PORT = 12345; // Default port
    public static final int[] DESTINATION_PORTS = { 12346, 12347 }; // Different ports for different destinations

    public static void main(String[] args) {
        Thread senderThread = new Thread(() -> startSender());
        Thread receiverThread = new Thread(() -> startReceiver());

        senderThread.start();
        receiverThread.start();
    }

    public static void startSender() {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
            DataLine.Info microphoneInfo = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(microphoneInfo);
            microphone.open(format);
            microphone.start();

            // List of destination IP addresses
            List<String> destinationIPs = new ArrayList<>();
            destinationIPs.add("192.168.29.128"); // Add multiple IP addresses here
            destinationIPs.add("192.168.29.157");

            try (DatagramSocket socket = new DatagramSocket()) {
                System.out.println("VoiceSender: Ready to send voice...");

                byte[] buffer = new byte[1024];
                DatagramPacket packet;

                while (true) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    for (int i = 0; i < destinationIPs.size(); i++) {
                        String ip = destinationIPs.get(i);
                        int port = DESTINATION_PORTS[i]; // Get the corresponding port for the IP
                        packet = new DatagramPacket(buffer, bytesRead, InetAddress.getByName(ip), port);
                        socket.send(packet);
                    }
                }
            }
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void startReceiver() {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(format);
            speaker.start();

            try (DatagramSocket socket = new DatagramSocket(DEFAULT_PORT)) {
                System.out.println("VoiceReceiver: Ready to receive voice...");

                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (true) {
                    socket.receive(packet);
                    speaker.write(packet.getData(), 0, packet.getLength());
                }
            }
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
}
