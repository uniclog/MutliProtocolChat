package local.uniclog.server;

import lombok.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerApp {
    private final Integer port = 27001;
    UdpNoBlockedSocketServerServiceThread udpServer = new UdpNoBlockedSocketServerServiceThread();
    TcpServerServiceThread tcpServer = new TcpServerServiceThread();
    private List<ClientModel> udpClients = Collections.synchronizedList(new ArrayList<>());
    private List<Connection> tcpClients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.err.println("Server starting...");
        new ServerApp().execute();
    }

    public void execute() {
        try {
            udpServer.init();
            udpServer.start();
            tcpServer.init();
            tcpServer.start();
        } catch (IOException e) {
            System.out.println("Udp Server start error");
        }
    }

    public void sendMessageToAll(String msg) {
        udpClients.stream().map(ClientModel::getAddress).forEach(c -> {
            try {
                udpServer.sendMessageToClient(msg, c);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });

        tcpClients.forEach(client -> {
            client.printWriter.println(msg);
            System.out.printf("TCP: send - %s (%s)%n", msg, client.socket);
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////    TCP    //////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class TcpServerServiceThread extends Thread {
        private final AtomicBoolean loop = new AtomicBoolean(false);
        private ServerSocket finalTcpServerSocket;

        public void setLoop(Boolean loop) {
            this.loop.set(loop);
        }

        private void init() throws IOException {
            this.loop.set(true);
            this.finalTcpServerSocket = new ServerSocket(port);
            tcpClients = Collections.synchronizedList(new ArrayList<>());
        }

        @Override
        public void run() {
            System.out.println("tcp: waiting for clients...");
            while (loop.get()) {
                try {
                    Socket socket = finalTcpServerSocket.accept();
                    Connection con = new Connection(socket);
                    tcpClients.add(con);
                    System.out.println("Add TCP client - " + con.socket.toString());
                    con.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class Connection extends Thread {
        private final Socket socket;
        private BufferedReader bufferedReader;
        private PrintWriter printWriter;
        private boolean isDisconnect = false;

        Connection(Socket socket) {
            this.socket = socket;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                printWriter = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            String str;
            while (!isDisconnect) {
                try {
                    str = bufferedReader.readLine();
                    if (str.equals("exit")) break;
                    System.out.println("\n TCP: receive - " + str + " from: " + socket.toString());

                    //send to all
                    sendMessageToAll(str);
                } catch (IOException e) {
                    try {
                        bufferedReader.close();
                        printWriter.close();
                    } catch (IOException ignore) {
                    }
                    isDisconnect = true;
                    e.printStackTrace();
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////    UDP    //////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class UdpNoBlockedSocketServerServiceThread extends Thread {

        private static final int BUFFER_SIZE = 1024;
        private final AtomicBoolean loop = new AtomicBoolean(false);
        private DatagramChannel socket;

        public void setLoop(boolean loop) {
            this.loop.set(loop);
        }

        private void init() throws IOException {
            this.loop.set(true);
            this.socket = DatagramChannel.open();
            this.socket.configureBlocking(false);
            this.socket.bind(new InetSocketAddress(port));
            udpClients = Collections.synchronizedList(new ArrayList<>());
        }

        @SneakyThrows
        @Override
        public void run() {
            System.out.println("udp: waiting for clients...");
            var buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (loop.get()) {
                buffer.clear();
                var address = socket.receive(buffer);
                if (address != null) {
                    buffer.flip();
                    var message = new String(buffer.array(), 0, buffer.limit());
                    ClientModel client = getConnectionInfo(address);
                    if (!udpClients.contains(client)) {
                        udpClients.add(client);
                        System.out.println("Add UDP client - " + client.getIp() + ":" + client.getPort());
                    }
                    System.out.println("\n UDP: receive - " + message + " from: " + client.getIp() + ":" + client.getPort());

                    // send
                    sendMessageToAll(message);
                }
                TimeUnit.MILLISECONDS.sleep(200);
            }

            this.socket.close();
        }

        public ClientModel getConnectionInfo(SocketAddress address) {
            String[] info = address.toString().split(":");
            return ClientModel.builder().ip(info[0]).port(Integer.parseInt(info[1])).address(address).build();
        }

        public void sendMessageToClient(String msg, SocketAddress address) throws IOException {
            socket.send(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)), address);
            System.out.printf("UDP: send - %s : %s%n", msg, address.toString());
        }
    }
}


