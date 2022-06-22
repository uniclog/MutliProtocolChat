package local.uniclog;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

import static javafx.stage.StageStyle.UTILITY;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        var loader = new FXMLLoader(getClass().getResource("view.fxml"));
        var root = (Parent) loader.load();

        var delta = new Point(0, 0);
        root.setOnMousePressed(mouseEvent -> {
            root.requestFocus();
            delta.setLocation(stage.getX() - mouseEvent.getScreenX(), stage.getY() - mouseEvent.getScreenY());
        });
        root.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() + delta.getX());
            stage.setY(mouseEvent.getScreenY() + delta.getY());
        });
        var scene = new Scene(root);
        stage.setOpacity(0.90);
        stage.initStyle(UTILITY);
        stage.setTitle("MutliProtocolChat");
        // stage.setAlwaysOnTop(true)
        stage.toFront();

        stage.setScene(scene);
        stage.show();
    }
}