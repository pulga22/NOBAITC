package me.julionxn.nobaitc.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.julionxn.nobaitc.MainApplication;
import me.julionxn.nobaitc.lib.MatlabFunctions;
import me.julionxn.nobaitc.lib.NONBPAGeneratorService;
import me.julionxn.nobaitc.models.FractionResult;
import me.julionxn.nobaitc.util.ClipboardHelper;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NONBPAController implements Initializable {

    @FXML private VBox factorsInputContainer;
    @FXML private Button addFactorButton;
    @FXML private Button removeFactorButton;
    @FXML private TextField fractionSizeField;
    @FXML private TextField numberOfFractionsField;
    @FXML private RadioButton randomFractionsRadio;
    @FXML private RadioButton customFractionsRadio;
    @FXML private TextField customFractionsField;
    @FXML private Button generateButton;
    @FXML private Button clearButton;

    @FXML private Label trLabel;
    @FXML private Label factorsCountLabel;
    @FXML private Label lcmLabel;
    @FXML private Label glLabel;
    @FXML private Label sfMinLabel;

    @FXML private TableView<FractionResult> resultsTable;
    @FXML private TableColumn<FractionResult, Integer> fractionNumberColumn;
    @FXML private TableColumn<FractionResult, String> fractionDataColumn;
    @FXML private TableColumn<FractionResult, Double> gbmColumn;
    @FXML private TableColumn<FractionResult, Double> j2Column;
    @FXML private TableColumn<FractionResult, Double> vifsColumn;
    @FXML private TextArea logTextArea;

    private NONBPAGeneratorService generatorService;
    private ObservableList<FractionResult> fractionResults;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generatorService = new NONBPAGeneratorService();
        fractionResults = FXCollections.observableArrayList();

        setupUI();
        setupTableColumns();
        setupValidation();
        addInitialFactors();
    }

    private void setupUI() {
        // Configurar radio buttons
        ToggleGroup fractionTypeGroup = new ToggleGroup();
        randomFractionsRadio.setToggleGroup(fractionTypeGroup);
        customFractionsRadio.setToggleGroup(fractionTypeGroup);
        randomFractionsRadio.setSelected(true);

        // Configurar tabla de resultados
        resultsTable.setItems(fractionResults);
        resultsTable.setRowFactory(val -> {
            TableRow<FractionResult> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    FractionResult rowData = row.getItem();
                    copyFractionToClipboard(rowData);
                    openDetailsWindow(rowData);
                }
            });

            return row;
        });

        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);

        customFractionsRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            customFractionsField.setDisable(!newVal);
        });
        customFractionsField.setDisable(true);
    }

    private void copyFractionToClipboard(FractionResult fractionResult){
        StringBuilder sb = new StringBuilder();
        double[][] data = fractionResult.getFraction();
        for (double[] row : data) {
            for (int i = 0; i < row.length; i++) {
                sb.append(row[i]);
                String toAppend = i != row.length - 1 ? "\t" : "\n";
                sb.append(toAppend);
            }
        }
        ClipboardHelper.copyToClipboard(sb.toString());
    }

    private void openDetailsWindow(FractionResult data) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.getResourceURL("fxml/fraction-result-details.fxml"));
            Parent root = loader.load();

            FractionResultDetailsController controller = loader.getController();
            controller.setData(data);

            Stage stage = new Stage();
            stage.setTitle("Details");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        fractionNumberColumn.setCellValueFactory(new PropertyValueFactory<>("fractionNumber"));
        fractionDataColumn.setCellValueFactory(new PropertyValueFactory<>("fractionData"));
        gbmColumn.setCellValueFactory(new PropertyValueFactory<>("gbm"));
        j2Column.setCellValueFactory(new PropertyValueFactory<>("j2"));
        vifsColumn.setCellValueFactory(new PropertyValueFactory<>("vifsData"));

        // Formatear columnas numéricas
        gbmColumn.setCellFactory(tc -> new TableCell<FractionResult, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.4f", value));
                }
            }
        });

        j2Column.setCellFactory(tc -> new TableCell<FractionResult, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.4f", value));
                }
            }
        });
    }

    private void setupValidation() {
        fractionSizeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                fractionSizeField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            updateDesignInfo();
        });

        numberOfFractionsField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                numberOfFractionsField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void addInitialFactors() {
        addFactorInput();
        addFactorInput();
    }

    @FXML
    private void addFactorInput() {
        HBox factorBox = new HBox(10);
        factorBox.getStyleClass().add("factor-input-box");

        Label label = new Label("Factor " + (factorsInputContainer.getChildren().size() + 1) + ":");
        label.setMinWidth(80);

        TextField levelField = new TextField();
        levelField.setPromptText("Niveles");
        levelField.setPrefWidth(100);

        // Validación numérica
        levelField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                levelField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            updateDesignInfo();
        });

        factorBox.getChildren().addAll(label, levelField);
        factorsInputContainer.getChildren().add(factorBox);

        updateRemoveButtonState();
        updateDesignInfo();
    }

    @FXML
    private void removeFactorInput() {
        if (factorsInputContainer.getChildren().size() > 1) {
            factorsInputContainer.getChildren().remove(factorsInputContainer.getChildren().size() - 1);
            updateRemoveButtonState();
            updateDesignInfo();
        }
    }

    private void updateRemoveButtonState() {
        removeFactorButton.setDisable(factorsInputContainer.getChildren().size() <= 1);
    }

    private void updateDesignInfo() {
        try {
            int[] design = getDesignArray();
            if (design.length == 0) {
                clearDesignInfo();
                return;
            }

            int tr = Arrays.stream(design).reduce(1, (a, b) -> a * b);
            int factors = design.length;
            int lcm = MatlabFunctions.calculateLCM(design);
            int gl = factors + 2;
            int maxLevel = Arrays.stream(design).max().orElse(0);
            int sfMin = Math.max(gl, maxLevel);

            trLabel.setText("Número de corridas (TR): " + tr);
            factorsCountLabel.setText("Factores: " + factors);
            lcmLabel.setText("Mínimo común múltiplo (LCM): " + lcm);
            glLabel.setText("Grados de libertad (GL): " + gl);
            sfMinLabel.setText("SF min: " + sfMin);

            // Validar si es un diseño válido
            boolean isValid = factors <= 9 && tr == lcm;
            String status = isValid ? "Válido" : "No válido (TR ≠ LCM o > 9 factores)";
            logTextArea.setText("Estado del diseño: " + status);

        } catch (Exception e) {
            clearDesignInfo();
        }
    }

    private void clearDesignInfo() {
        trLabel.setText("Número de corridas (TR): -");
        factorsCountLabel.setText("Factores: -");
        lcmLabel.setText("Mínimo común múltiplo (LCM): -");
        glLabel.setText("Grados de libertad (GL): -");
        sfMinLabel.setText("SF min: -");
    }

    @FXML
    private void generateFractions() {
        try {
            // Validar entrada
            int[] design = getDesignArray();
            if (design.length == 0) {
                showError("Error", "Ingrese al menos un factor con niveles válidos");
                return;
            }

            int fractionSize = Integer.parseInt(fractionSizeField.getText());
            int numberOfFractions = Integer.parseInt(numberOfFractionsField.getText());

            // Generar fracciones usando el servicio
            List<FractionResult> results;
            if (randomFractionsRadio.isSelected()) {
                results = generatorService.generateRandomFractions(design, fractionSize, numberOfFractions);
            } else {
                String customInput = customFractionsField.getText();
                List<Integer> customFractions = parseCustomFractions(customInput);
                results = generatorService.generateCustomFractions(design, fractionSize, customFractions);
            }

            // Mostrar resultados
            fractionResults.clear();
            fractionResults.addAll(results);

            // Log de resultados
            StringBuilder log = new StringBuilder();
            log.append("Generación completada exitosamente!\n\n");
            log.append("Resumen de fracciones generadas:\n");
            for (FractionResult result : results) {
                log.append(String.format("Fracción %d - GBM: %.4f, J2: %.4f\n",
                        result.getFractionNumber(), result.getGbm(), result.getJ2()));
            }
            logTextArea.setText(log.toString());

        } catch (NumberFormatException e) {
            showError("Error de entrada", "Verifique que todos los campos numéricos sean válidos");
        } catch (Exception e) {
            showError("Error", "Error al generar fracciones: " + e.getMessage());
        }
    }

    @FXML
    private void clearResults() {
        fractionResults.clear();
        logTextArea.clear();

        // Limpiar campos de entrada
        factorsInputContainer.getChildren().clear();
        fractionSizeField.clear();
        numberOfFractionsField.clear();
        customFractionsField.clear();

        addInitialFactors();
        clearDesignInfo();
    }

    private int[] getDesignArray() {
        return factorsInputContainer.getChildren().stream()
                .map(node -> (HBox) node)
                .map(hbox -> (TextField) hbox.getChildren().get(1))
                .map(TextField::getText)
                .filter(text -> !text.isEmpty())
                .mapToInt(Integer::parseInt)
                .filter(level -> level > 0)
                .toArray();
    }

    private List<Integer> parseCustomFractions(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo de fracciones personalizadas vacío");
        }

        String cleaned = input.replaceAll("[\\[\\]]", "").trim();
        return Arrays.stream(cleaned.split("[,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}