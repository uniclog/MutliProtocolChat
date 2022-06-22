package local.uniclog.netcore;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

class UdpClient {
    private static DatagramSocket udpClientSocket;
    private final int port = 27001;
    private final String host = "localhost";
    private InetAddress address;
    private UDPReceiverThread udpReceiverThread;

    public static void main(String[] args) {
        UdpClient client = new UdpClient();
        client.init();
    }

    @SneakyThrows
    public void init() {
        try {
            address = InetAddress.getByName(host);
            udpClientSocket = new DatagramSocket();
            udpClientSocket.connect(address, port);
            UDPSenderThread sender = new UDPSenderThread(address, port, "hello msg");
            sender.start();
            udpReceiverThread = new UDPReceiverThread(udpClientSocket);
            udpReceiverThread.start();

            System.out.println("udp client init...");

            String message = "hello all";
            DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), 0, message.length(), address, port);
            udpClientSocket.send(sendPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    static class UDPSenderThread extends Thread {
        private final InetAddress serverIPAddress;
        private final int port;
        private final String msg;

        UDPSenderThread(InetAddress address, int serverport, String msg) {
            this.serverIPAddress = address;
            this.port = serverport;
            this.msg = msg;

        }

        @Override
        public void run() {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String message = null;
            try {
                while ((message = bufferedReader.readLine()) != null) {
                    // отправка сообщения
                    DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), 0, message.length(), serverIPAddress, port);
                    udpClientSocket.send(sendPacket);
                    System.out.println("Send:" + message);
                    if (message.equals("exit")) {
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    static class UDPReceiverThread extends Thread {
        private final DatagramSocket udpClientSocket;
        private boolean stopped = false;

        UDPReceiverThread(DatagramSocket ds) {
            this.udpClientSocket = ds;
        }

        @Override
        public void run() {
            byte[] receiveData = new byte[1024];
            while (!stopped) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    udpClientSocket.receive(receivePacket);
                    String serverReply = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("receive: " + serverReply);
                    Thread.yield();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}