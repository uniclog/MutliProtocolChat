package local.uniclog.netcore;

import local.uniclog.netcore.service.TCPService;
import local.uniclog.netcore.service.UDPService;

public class ServerStart {
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

    public static void main(String[] args) {
        new ServerStart();
    }
}
