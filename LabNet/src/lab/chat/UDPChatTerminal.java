
package lab.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UDPChatTerminal extends JFrame {

    private final DatagramSocket socket;
    private final InetAddress receiverAddress;
    private final int receiverPort;

    private final JTextArea chatArea = new JTextArea();
    private final JTextField messageField = new JTextField();
    private final JButton sendButton = new JButton("Send");

    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    private Thread senderThread;
    private Thread receiverThread;

    public UDPChatTerminal(int localPort, String ip, int receiverPort)
            throws IOException {

        this.socket = new DatagramSocket(localPort);
        this.receiverAddress = InetAddress.getByName(ip);
        this.receiverPort = receiverPort;

        // GUI
        setTitle("UDP Chat - Port " + localPort);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                socket.close();
                senderThread.interrupt();
            }
        });

        // Start the two threads
        startSenderThread();
        startReceiverThread();

        setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        messages.add(message);
        messageField.setText("");
    }

    // THREAD 1 - Sending messages
    private void startSenderThread() {
        senderThread = new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    String message = messages.take();

                    byte[] buffer = message.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket packet = new DatagramPacket(
                            buffer,
                            buffer.length,
                            receiverAddress,
                            receiverPort
                    );

                    socket.send(packet);
                    displayMessage("You: " + message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    displayMessage("Send error: " + e.getMessage());
                }
            }
        });

        senderThread.setDaemon(true);
        senderThread.start();
    }

    // THREAD 2 - Receiving messages
    private void startReceiverThread() {
        receiverThread = new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    byte[] buffer = new byte[1024];

                    DatagramPacket packet =
                            new DatagramPacket(buffer, buffer.length);

                    socket.receive(packet);

                    String message = new String(
                            packet.getData(),
                            packet.getOffset(),
                            packet.getLength(),
                            StandardCharsets.UTF_8
                    );

                    displayMessage("Peer: " + message);

                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        displayMessage("Receive error: " + e.getMessage());
                    }
                }
            }
        });

        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    private void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println(
                    "Usage: UDPChatTerminal <localPort> <receiverIP> <receiverPort>"
            );
            return;
        }

        try {
            int localPort = Integer.parseInt(args[0]);
            String ip = args[1];
            int receiverPort = Integer.parseInt(args[2]);

            SwingUtilities.invokeLater(() -> {
                try {
                    new UDPChatTerminal(localPort, ip, receiverPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (NumberFormatException e) {
            System.out.println("Ports must be valid numbers.");
        }
    }
}
