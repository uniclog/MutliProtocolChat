package local.uniclog.server.service;

import local.uniclog.server.service.interfaces.Protocol;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TCPService extends Thread implements Protocol {
    private final AtomicBoolean loop = new AtomicBoolean(false);
    private ServerSocket finalTcpServerSocket;
    private List<Connection> tcpClients = Collections.synchronizedList(new ArrayList<>());
    private Consumer<String> callback;

    public void setLoop(Boolean loop) {
        this.loop.set(loop);
    }

    public List<Connection> getTcpClients() {
        return tcpClients;
    }

    public void init(Consumer<String> callback) throws IOException {
        this.callback = callback;
        this.loop.set(true);
        this.finalTcpServerSocket = new ServerSocket(Protocol.port);
        tcpClients = Collections.synchronizedList(new ArrayList<>());
    }

    public void sendMessageToAllClients(String message) {
        tcpClients.forEach(client -> {
            client.printWriter.println(message);
            System.out.printf("TCP: send - %s (%s)%n", message, client.socket);
        });
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

        @SneakyThrows
        @Override
        public void run() {
            String str;
            while (!isDisconnect) {
                try {
                    str = bufferedReader.readLine();
                    if (str.equals("exit")) {
                        System.out.printf("TCP: client - %s : lost connection%n", socket.toString());
                        tcpClients.remove(this);
                        break;
                    }
                    System.out.println("\n TCP: receive - " + str + " from: " + socket.toString());
                    // send to all
                    sendMessageToAllClients(str);
                    callback.accept(str);
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
}
