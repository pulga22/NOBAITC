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
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.julionxn.nobaitc.MainApplication;
import me.julionxn.nobaitc.data.nonbpa.NONBPAGeneratorService;
import me.julionxn.nobaitc.data.nonbpa.FractionResult;
import me.julionxn.nobaitc.util.ClipboardHelper;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controlador para la interfaz de generación de fracciones NONBPA.
 * Optimizado para mejor rendimiento y mantenibilidad.
 */
public class NONBPAController implements Initializable {

    // ==================== FXML Components ====================

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

    // ==================== Services & Data ====================

    private final NONBPAGeneratorService generatorService;
    private final ObservableList<FractionResult> fractionResults;

    // Constantes
    private static final int MIN_FACTORS = 1;
    private static final int INITIAL_FACTORS = 2;
    private static final int MAX_FACTORS = 9;
    private static final String NUMERIC_REGEX = "\\d*";
    private static final String NUMBER_FORMAT = "%.4f";

    public NONBPAController() {
        this.generatorService = new NONBPAGeneratorService();
        this.fractionResults = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupTableColumns();
        setupValidation();
        addInitialFactors();
        updateDesignInfo();
    }

    // ==================== UI Setup ====================

    private void setupUI() {
        setupRadioButtons();
        setupResultsTable();
        setupLogArea();
        setupCustomFractionsField();
    }

    private void setupRadioButtons() {
        ToggleGroup fractionTypeGroup = new ToggleGroup();
        randomFractionsRadio.setToggleGroup(fractionTypeGroup);
        customFractionsRadio.setToggleGroup(fractionTypeGroup);
        randomFractionsRadio.setSelected(true);
    }

    private void setupResultsTable() {
        resultsTable.setItems(fractionResults);
        resultsTable.setRowFactory(this::createTableRowFactory);
    }

    private TableRow<FractionResult> createTableRowFactory(TableView<FractionResult> tv) {
        TableRow<FractionResult> row = new TableRow<>();

        row.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !row.isEmpty()) {
                FractionResult rowData = row.getItem();
                handleFractionDoubleClick(rowData);
            }
        });

        return row;
    }

    private void handleFractionDoubleClick(FractionResult fractionResult) {
        copyFractionToClipboard(fractionResult);
        openDetailsWindow(fractionResult);
    }

    private void setupLogArea() {
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);
    }

    private void setupCustomFractionsField() {
        customFractionsRadio.selectedProperty().addListener((obs, oldVal, newVal) ->
                customFractionsField.setDisable(!newVal)
        );
        customFractionsField.setDisable(true);
    }

    private void setupTableColumns() {
        // Configurar value factories
        fractionNumberColumn.setCellValueFactory(new PropertyValueFactory<>("fractionNumber"));
        fractionDataColumn.setCellValueFactory(new PropertyValueFactory<>("fractionData"));
        gbmColumn.setCellValueFactory(new PropertyValueFactory<>("gbm"));
        j2Column.setCellValueFactory(new PropertyValueFactory<>("j2"));
        vifsColumn.setCellValueFactory(new PropertyValueFactory<>("vifsData"));

        // Formatear columnas numéricas
        setupNumericColumn(gbmColumn);
        setupNumericColumn(j2Column);
    }

    private void setupNumericColumn(TableColumn<FractionResult, Double> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format(NUMBER_FORMAT, value));
            }
        });
    }

    private void setupValidation() {
        addNumericValidation(fractionSizeField, true);
        addNumericValidation(numberOfFractionsField, false);
    }

    private void addNumericValidation(TextField field, boolean updateDesignInfo) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches(NUMERIC_REGEX)) {
                field.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (updateDesignInfo) {
                updateDesignInfo();
            }
        });
    }

    // ==================== Factor Management ====================

    private void addInitialFactors() {
        for (int i = 0; i < INITIAL_FACTORS; i++) {
            addFactorInput();
        }
    }

    @FXML
    private void addFactorInput() {
        if (factorsInputContainer.getChildren().size() >= MAX_FACTORS) {
            showWarning("Límite alcanzado", "No se pueden agregar más de " + MAX_FACTORS + " factores");
            return;
        }

        HBox factorBox = createFactorInputBox();
        factorsInputContainer.getChildren().add(factorBox);

        updateRemoveButtonState();
        updateDesignInfo();
    }

    private HBox createFactorInputBox() {
        int factorNumber = factorsInputContainer.getChildren().size() + 1;

        HBox factorBox = new HBox(10);
        factorBox.getStyleClass().add("factor-input-box");

        Label label = new Label("Factor " + factorNumber + ":");
        label.setMinWidth(80);

        TextField levelField = createLevelTextField();

        factorBox.getChildren().addAll(label, levelField);
        return factorBox;
    }

    private TextField createLevelTextField() {
        TextField levelField = new TextField();
        levelField.setPromptText("Niveles");
        levelField.setPrefWidth(100);

        levelField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches(NUMERIC_REGEX)) {
                levelField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            updateDesignInfo();
        });

        return levelField;
    }

    @FXML
    private void removeFactorInput() {
        if (factorsInputContainer.getChildren().size() > MIN_FACTORS) {
            factorsInputContainer.getChildren().remove(
                    factorsInputContainer.getChildren().size() - 1
            );
            updateRemoveButtonState();
            updateDesignInfo();
        }
    }

    private void updateRemoveButtonState() {
        removeFactorButton.setDisable(
                factorsInputContainer.getChildren().size() <= MIN_FACTORS
        );
    }

    // ==================== Design Info ====================

    private void updateDesignInfo() {
        try {
            int[] design = getDesignArray();

            if (design.length == 0) {
                clearDesignInfo();
                return;
            }

            NONBPAGeneratorService.DesignParameters params =
                    generatorService.calculateParameters(design);

            displayDesignParameters(params);
            displayValidationStatus(design, params);

        } catch (Exception e) {
            clearDesignInfo();
        }
    }

    private void displayDesignParameters(NONBPAGeneratorService.DesignParameters params) {
        trLabel.setText("Número de corridas (TR): " + params.tr());
        factorsCountLabel.setText("Factores: " + params.factors());
        lcmLabel.setText("Mínimo común múltiplo (LCM): " + params.lcm());
        glLabel.setText("Grados de libertad (GL): " + params.gl());
        sfMinLabel.setText("SF min: " + params.sfMin());
    }

    private void displayValidationStatus(int[] design, NONBPAGeneratorService.DesignParameters params) {
        boolean isValid = generatorService.validateDesign(design);
        String status = isValid ?
                "Diseño válido" :
                "Diseño no válido (TR ≠ LCM o > " + MAX_FACTORS + " factores)";

        logTextArea.setText("Estado: " + status);
    }

    private void clearDesignInfo() {
        trLabel.setText("Número de corridas (TR): -");
        factorsCountLabel.setText("Factores: -");
        lcmLabel.setText("Mínimo común múltiplo (LCM): -");
        glLabel.setText("Grados de libertad (GL): -");
        sfMinLabel.setText("SF min: -");
        logTextArea.clear();
    }

    // ==================== Fraction Generation ====================

    @FXML
    private void generateFractions() {
        try {
            int[] design = validateAndGetDesign();
            int fractionSize = parseIntegerField(fractionSizeField, "Tamaño de fracción");
            int numberOfFractions = parseIntegerField(numberOfFractionsField, "Número de fracciones");

            List<FractionResult> results = generateFractionsBasedOnMode(
                    design, fractionSize, numberOfFractions
            );

            displayResults(results);

        } catch (NumberFormatException e) {
            showError("Error de entrada", "Verifique que todos los campos numéricos sean válidos");
        } catch (IllegalArgumentException e) {
            showError("Error de validación", e.getMessage());
        } catch (Exception e) {
            showError("Error", "Error al generar fracciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int[] validateAndGetDesign() {
        int[] design = getDesignArray();

        if (design.length == 0) {
            throw new IllegalArgumentException("Ingrese al menos un factor con niveles válidos");
        }

        if (!generatorService.validateDesign(design)) {
            throw new IllegalArgumentException("El diseño no es válido para NONBPA");
        }

        return design;
    }

    private int parseIntegerField(TextField field, String fieldName) {
        String text = field.getText().trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " no puede estar vacío");
        }

        return Integer.parseInt(text);
    }

    private List<FractionResult> generateFractionsBasedOnMode(
            int[] design, int fractionSize, int numberOfFractions) {

        if (randomFractionsRadio.isSelected()) {
            return generatorService.generateRandomFractions(
                    design, fractionSize, numberOfFractions
            );
        } else {
            List<Integer> customFractions = parseCustomFractions(
                    customFractionsField.getText()
            );
            return generatorService.generateCustomFractions(
                    design, fractionSize, customFractions
            );
        }
    }

    private void displayResults(List<FractionResult> results) {
        fractionResults.clear();
        fractionResults.addAll(results);

        logTextArea.setText(buildResultsSummary(results));
    }

    private String buildResultsSummary(List<FractionResult> results) {
        StringBuilder log = new StringBuilder();
        log.append("Generación completada exitosamente!\n\n");
        log.append("Resumen de fracciones generadas:\n");
        log.append("─".repeat(50)).append("\n");

        for (FractionResult result : results) {
            log.append(String.format(
                    "Fracción %d - GBM: %.4f, J2: %.4f, Max VIF: %.4f\n",
                    result.getFractionNumber(),
                    result.getGbm(),
                    result.getJ2(),
                    Arrays.stream(result.getVifs()).max().orElse(0.0)
            ));
        }

        log.append("─".repeat(50)).append("\n");
        log.append("Total: ").append(results.size()).append(" fracciones\n");
        log.append("\nDoble clic en una fila para ver detalles y copiar al portapapeles");

        return log.toString();
    }

    @FXML
    private void clearResults() {
        fractionResults.clear();
        logTextArea.clear();

        fractionSizeField.clear();
        numberOfFractionsField.clear();
        customFractionsField.clear();

        factorsInputContainer.getChildren().clear();
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

        String cleaned = input.replaceAll("[\\[\\]\\s]", "");

        return Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private void copyFractionToClipboard(FractionResult fractionResult) {
        double[][] data = fractionResult.getFraction();
        StringBuilder sb = new StringBuilder();

        for (double[] row : data) {
            for (int i = 0; i < row.length; i++) {
                sb.append(row[i]);
                sb.append(i != row.length - 1 ? "\t" : "\n");
            }
        }

        ClipboardHelper.copyToClipboard(sb.toString());
    }

    private void openDetailsWindow(FractionResult data) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApplication.getResourceURL("fxml/fraction-result-details.fxml")
            );
            Parent root = loader.load();

            FractionResultDetailsController controller = loader.getController();
            controller.setData(data);

            Stage stage = new Stage();
            stage.setTitle("Detalles - Fracción " + data.getFractionNumber());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            showError("Error", "No se pudo abrir la ventana de detalles");
            e.printStackTrace();
        }
    }

    // ==================== Alert Methods ====================

    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type.toString());
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}