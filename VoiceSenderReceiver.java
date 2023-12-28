import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class VoiceSenderReceiver {
    public static final int PORT = 8888; // Choose a port number

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
            destinationIPs.add("192.168.29.158"); // Add multiple IP addresses here
	    destinationIPs.add("192.168.29.157");
            try (DatagramSocket socket = new DatagramSocket()) {
                System.out.println("VoiceSender: Ready to send voice...");

                byte[] buffer = new byte[1024];
                DatagramPacket packet;

                while (true) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    for (String ip : destinationIPs) {
                        packet = new DatagramPacket(buffer, bytesRead, InetAddress.getByName(ip), PORT);
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

            try (DatagramSocket socket = new DatagramSocket(PORT)) {
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
