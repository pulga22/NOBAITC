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

    private void setupModuleButtons() {
        // Crear botones para cada módulo
        Button nonbpaButton = createModuleButton(
                "Generador NONBPA",
                "Genera fracciones semi-balanceadas\npara diseños de niveles mixtos",
                this::loadNONBPAModule
        );

        Button futureModuleButton = createModuleButton(
                "Otro",
                "Próximamente disponible",
                this::showComingSoon
        );
        futureModuleButton.setDisable(true);

        moduleButtonsContainer.getChildren().addAll(nonbpaButton, futureModuleButton);
    }

    private Button createModuleButton(String title, String description, Runnable action) {
        Button button = new Button();
        button.setText(title);
        button.setTooltip(new Tooltip(description));
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("module-button");
        button.setOnAction(e -> action.run());
        return button;
    }

    @FXML
    private void loadNONBPAModule() {
        try {
            infoTextArea.setText("""
                Módulo: Generador NONBPA
                
                Este módulo genera fracciones semi-balanceadas y semi-ortogonales
                para diseños factoriales de niveles mixtos puros.
                
                Características:
                • Soporta de 2 a 9 factores
                • Calcula métricas GBM y J2
                • Permite fracciones aleatorias o personalizadas
                """);

            FXMLLoader loader = new FXMLLoader(MainApplication.getResourceURL("fxml/nonbpa-view.fxml"));
            mainBorderPane.setCenter(loader.load());
        } catch (IOException e) {
            showError("Error al cargar el módulo NONBPA", e.getMessage());
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