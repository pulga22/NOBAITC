package me.julionxn.nobaitc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getResourceURL("fxml/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

        // Configuración de la ventana principal
        stage.setTitle("Diseño Experimental - Generador de Fracciones");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static URL getResourceURL(String path) {
        return MainApplication.class.getResource(path);
    }

}