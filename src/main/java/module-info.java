module com.example.uefamediaplayer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.media;

    opens com.example.uefamediaplayer to javafx.fxml;
    exports com.example.uefamediaplayer;
}
