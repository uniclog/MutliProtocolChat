package local.uniclog.netcore.service.interfaces;

import java.io.IOException;
import java.util.function.Consumer;

public interface Protocol {
    int port = 27001;

    //void init() throws IOException;

    void init(Consumer<String> callback) throws IOException;

    //void sendMessageAllClients(String message);
}
