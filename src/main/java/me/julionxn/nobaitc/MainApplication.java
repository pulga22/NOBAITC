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
                {1, 1, 1, 4},//1
                {1, 2, 4, 5},//2
                {1, 3, 4, 2},//3
                {1, 1, 2, 7},//4
                {1, 2, 4, 6},//5
                {1, 3, 5, 1},//6
                {1, 1, 4, 3},//7
                {2, 2, 4, 4},//8
                {2, 3, 5, 5},//9
                {2, 1, 3, 6},//10
                {2, 2, 5, 7},//11
                {2, 3, 5, 3},//12
                {2, 1, 2, 1},//13
                {2, 2, 4, 2},//14
                {2, 3, 5, 7},//15
                {3, 1, 3, 5},//16
                {3, 2, 4, 1},//17
                {3, 3, 5, 6},//18
                {3, 1, 1, 2},//19
                {3, 2, 4, 3},//20
                {3, 3, 5, 4} //21
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

        AliasStructureGenerator structureGenerator = new AliasStructureGenerator(matrix);
        AliasStructureGenerator.AliasStructure structure = structureGenerator.generate();
        structure.print();

    }

}