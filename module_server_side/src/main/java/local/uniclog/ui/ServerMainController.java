package local.uniclog.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import local.uniclog.netcore.ServerStart;

import static javafx.application.Platform.runLater;

public class ServerMainController {

    private ServerStart server;
    @FXML
    private TextArea console;

    public void initialize() {
        server = new ServerStart();
        // TODO добавить поддержку отображения ip
    }

    public void startAction() {
        server.actionStart(this::addToConsole);
    }

    public void stopAction() {
        addToConsole("not release ...");
        // TODO добавить остановку серверов
    }

    public void addToConsole(String text) {
        runLater(() -> console.setText(String.format("%s%n%s", console.getText(), text)));
    }
}
