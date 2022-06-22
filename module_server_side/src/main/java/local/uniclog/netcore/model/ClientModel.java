package local.uniclog.netcore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.net.SocketAddress;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ClientModel {
    private String ip;
    private Integer port;
    private String name;

    private SocketAddress address;

    @Override
    public String toString() {
        return "ClientModel{" +
                "name='" + name + '\'' +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}