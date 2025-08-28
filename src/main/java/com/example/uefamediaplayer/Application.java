package com.example.uefamediaplayer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("uefaMediaPlayer.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());

        stage.setTitle("UEFA MEDIAPLAYER");
        uefaMediaPlayerController controller = fxmlLoader.getController();
        handleKeys(scene, controller);

        stage.setScene(scene);
        setStage(stage);
    }
    void setStage(Stage stage) {
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.show();
    }

    void handleKeys(Scene scene, uefaMediaPlayerController controller) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case SPACE:
                    controller.pauseMedia();
                    event.consume();
                    break;
                case LEFT:
                    controller.seekSliderBy(-0.5);
                    event.consume();
                    break;
                case RIGHT:
                    controller.seekSliderBy(0.5);
                    event.consume();
                    break;
                case I:
                    controller.showInfo();
                    event.consume();
                    break;
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}