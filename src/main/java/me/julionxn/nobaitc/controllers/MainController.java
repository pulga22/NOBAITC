package me.julionxn.nobaitc.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import me.julionxn.nobaitc.MainApplication;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private BorderPane mainBorderPane;
    @FXML private VBox moduleButtonsContainer;
    @FXML private Label welcomeLabel;
    @FXML private TextArea infoTextArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupWelcomeScreen();
        setupModuleButtons();
    }

    private void setupWelcomeScreen() {
        welcomeLabel.setText("Bienvenido al Sistema NOBA-ITC");

        String welcomeText = """
                Módulos disponibles:
                - Generador NONBPA
                
                Selecciona un módulo del panel izquierdo para comenzar.
                """;

        infoTextArea.setText(welcomeText);
        infoTextArea.setEditable(false);
        infoTextArea.setWrapText(true);
    }

    // Crear botones para cada módulo
    private void setupModuleButtons() {
        Button nonbpaButton = createModuleButton(
                "NONBPA",
                "Genera fracciones semi-balanceadas\nsemi-ortogonales",
                """
                Módulo: Experimental Designs Tools
                
                Este método aplica para los diseños de niveles mixtos puros,
                estos tiene como característica principal que el tamaño de la matriz del modelo (TR)
                es igual al mínimo común múltiplo (LCM) de los niveles y poseen la capacidad para
                generar fracciones semi-balanceadas semi-ortogonales.
                Este modulo esta programado para generar fracciones de diseños que poseen de 2 a 9 factores.
                """,
                this::loadNONBPAModule
        );

        Button aliasButton = createModuleButton(
                "Alias Structure",
                "...",
                "...",
                this::loadAliasModule
        );

        Button futureModuleButton = createModuleButton(
                "Otro",
                "Próximamente disponible",
                "Próximamente disponible",
                this::showComingSoon
        );
        futureModuleButton.setDisable(true);

        moduleButtonsContainer.getChildren().addAll(nonbpaButton, aliasButton, futureModuleButton);
    }

    private Button createModuleButton(String title, String smallDescription, String moduleDescription, Runnable action) {
        Button button = new Button();
        button.setText(title);
        button.setTooltip(new Tooltip(smallDescription));
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("module-button");
        button.setOnAction(e -> action.run());
        button.setOnMouseEntered(e -> infoTextArea.setText(moduleDescription));
        return button;
    }

    @FXML
    private void loadNONBPAModule() {
        loadModule("nonbpa-view.fxml");
    }

    private void loadAliasModule() {
        loadModule("alias-structure-view.fxml");
    }

    private void loadModule(String viewFile){
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.getResourceURL("fxml/" + viewFile));
            mainBorderPane.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar el módulo " + viewFile, e.getMessage());
        }
    }

    private void showComingSoon() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Próximamente");
        alert.setHeaderText("Módulo en desarrollo");
        alert.setContentText("Este módulo estará disponible en futuras versiones.");
        alert.showAndWait();
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Sistema de Diseño Experimental");
        alert.setContentText("""
            Versión: 1.0.0
            
            Sistema para análisis y generación de diseños experimentales.
            Desarrollado para facilitar la creación de fracciones factoriales
            con métricas de balance y ortogonalidad.
            """);
        alert.showAndWait();
    }

    @FXML
    private void exitApplication() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Salir");
        alert.setHeaderText("¿Está seguro que desea salir?");
        alert.setContentText("Se perderán los datos no guardados.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.exit(0);
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}