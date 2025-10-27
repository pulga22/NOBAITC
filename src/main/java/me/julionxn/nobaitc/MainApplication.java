package me.julionxn.nobaitc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.julionxn.nobaitc.lib.AliasStructureGenerator;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        testAlias();
        FXMLLoader fxmlLoader = new FXMLLoader(getResourceURL("fxml/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
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

    private static void testAlias(){

        final double[][] matrix = {
                {1, 1, 1, 4},
                {1, 2, 4, 5},
                {1, 3, 4, 2},
                {1, 1, 2, 7},
                {1, 2, 4, 6},
                {1, 3, 5, 1},
                {1, 1, 4, 3},
                {2, 2, 4, 4},
                {2, 3, 5, 5},
                {2, 1, 3, 6},
                {2, 2, 5, 7},
                {2, 3, 5, 3},
                {2, 1, 2, 1},
                {2, 2, 4, 2},
                {2, 3, 5, 7},
                {3, 1, 3, 5},
                {3, 2, 4, 1},
                {3, 3, 5, 6},
                {3, 1, 1, 2},
                {3, 2, 4, 3},
                {3, 3, 5, 4}
        };

        final double[][] matrix2 = {
                {1, 1, 1, 4},
                {1, 2, 4, 5},
                {1, 3, 4, 2},
                {1, 1, 2, 7},
                {1, 2, 4, 6},
                {1, 3, 5, 1},
                {1, 1, 4, 3},
                {2, 2, 4, 4},
        };

        AliasStructureGenerator structureGenerator = new AliasStructureGenerator(matrix2);
        AliasStructureGenerator.AliasStructure structure = structureGenerator.generate();
        structure.print();

    }

}