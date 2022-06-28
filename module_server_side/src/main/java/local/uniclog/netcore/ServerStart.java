package local.uniclog.netcore;

import local.uniclog.netcore.service.TCPService;
import local.uniclog.netcore.service.UDPService;

import java.util.Objects;
import java.util.function.Consumer;

public class ServerStart {
    public static void main(String[] args) {
        new ServerStart().actionStart(null);
    }

    public void actionStart(Consumer<String> callback) {
        UDPService udpServer = new UDPService(callback);
        TCPService tcpServer = new TCPService(callback);

        sendMessageToConsole(callback, "TCP server starting ...");
        try {
            tcpServer.init(udpServer::sendMessageAllClients);
            tcpServer.start();
        } catch (Exception e) {
            sendMessageToConsole(callback, "Error starting tcp server");
        }
        sendMessageToConsole(callback, "UDP server starting ...");
        try {
            udpServer.init(tcpServer::sendMessageToAllClients);
            udpServer.start();
        } catch (Exception e) {
            sendMessageToConsole(callback, "Error starting udp server");
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public void sendMessageToConsole(Consumer<String> callback, String message) {
        System.out.println(message);
        if (Objects.nonNull(callback)) {
            callback.accept(message);
        }
    }
}
