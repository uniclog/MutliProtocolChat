package local.uniclog.netcore.service;

import local.uniclog.netcore.model.ClientModel;
import local.uniclog.netcore.service.interfaces.Protocol;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class UDPService extends Thread implements Protocol {
    private static final int BUFFER_SIZE = 1024;
    private final AtomicBoolean loop = new AtomicBoolean(false);
    private final Consumer<String> textToConsole;
    private DatagramChannel socket;
    private List<ClientModel> udpClients = Collections.synchronizedList(new ArrayList<>());
    private Consumer<String> callback;

    public UDPService(Consumer<String> callback) {
        this.textToConsole = callback;
    }

    public void setLoop(boolean loop) {
        this.loop.set(loop);
    }

    public List<ClientModel> getUdpClients() {
        return udpClients;
    }

    public void init(Consumer<String> callback) throws IOException {
        this.callback = callback;
        this.loop.set(true);
        this.socket = DatagramChannel.open(); //Открывает сокет
        this.socket.configureBlocking(false); //Неблокируещий сокет
        this.socket.bind(new InetSocketAddress(Protocol.port)); //Привязывает сокет канала к локальному адресу.
    }

    @SneakyThrows
    @Override
    public void run() {
        sendMessageToConsole("udp: waiting for clients...");
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
                    sendMessageToConsole("Add UDP client - " + client.getIp() + ":" + client.getPort());
                }
                sendMessageToConsole("\n UDP: receive - " + message + " from: " + client.getIp() + ":" + client.getPort());

                if (message.equals("exit")) {
                    sendMessageToConsole(String.format("UDP: client - %s : lost connection%n", client.getAddress().toString()));
                    udpClients.remove(client);
                }
                // send to all
                sendMessageAllClients(message);
                callback.accept(message);
            }
            TimeUnit.MILLISECONDS.sleep(200);
        }

        this.socket.close();
    }

    public void sendMessageAllClients(String message) {
        udpClients.stream().forEach(client -> {
            try {
                sendMessageToClient(message, client);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public ClientModel getConnectionInfo(SocketAddress address) {
        String[] info = address.toString().split(":");
        return ClientModel.builder().ip(info[0]).port(Integer.parseInt(info[1])).address(address).build();
    }

    public void sendMessageToClient(String msg, ClientModel client) throws IOException {
        socket.send(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)), client.getAddress());
        sendMessageToConsole(String.format("UDP: send - %s : %s%n", msg, client.getAddress().toString()));
    }

    public void sendMessageToConsole(String message) {
        System.out.println(message);
        if (Objects.nonNull(textToConsole)) {
            textToConsole.accept(message);
        }
    }
}
