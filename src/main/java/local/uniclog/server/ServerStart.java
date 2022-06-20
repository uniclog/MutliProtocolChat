package local.uniclog.server;

import local.uniclog.server.service.TCPService;
import local.uniclog.server.service.UDPService;

public class ServerStart {
    public static void main(String[] args) {
        new ServerStart();
    }

    public ServerStart() {
        UDPService udpServer = new UDPService();
        TCPService tcpServer = new TCPService();

        System.out.println("TCP server starting ...");
        try {
            tcpServer.init(udpServer::sendMessageAllClients);
            tcpServer.start();
        } catch (Exception e) {
            System.out.println("Error starting tcp server");
        }
        System.out.println("UDP server starting ...");
        try {
            udpServer.init(tcpServer::sendMessageToAllClients);
            udpServer.start();
        } catch (Exception e) {
            System.out.println("Error starting udp server");
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
