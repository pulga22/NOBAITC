package me.julionxn.nobaitc.data.nonbpa;

import me.julionxn.nobaitc.data.MatlabFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para generar fracciones NONBPA
 */
public class NONBPAGeneratorService {

    private final BalancedGBMMatrix gbmCalculator;
    private final OrthogonalJ2Matrix j2Calculator;
    private final VIFSMatrix vifsCalculator;

    public NONBPAGeneratorService() {
        this.gbmCalculator = new BalancedGBMMatrix();
        this.j2Calculator = new OrthogonalJ2Matrix();
        this.vifsCalculator = new VIFSMatrix();
    }

    /**
     * Valida si un diseño es válido para NONBPA
     */
    public boolean validateDesign(int[] design) {
        if (design == null || design.length == 0 || design.length > 9) {
            return false;
        }

        int tr = calculateProduct(design);
        int lcm = MatlabFunctions.calculateLCM(design);

        return tr == lcm;
    }

    /**
     * Calcula parámetros del diseño
     */
    public DesignParameters calculateParameters(int[] design) {
        int tr = calculateProduct(design);
        int factors = design.length;
        int lcm = MatlabFunctions.calculateLCM(design);
        int gl = factors + 2;
        int maxLevel = Arrays.stream(design).max().orElse(0);
        int sfMin = Math.max(gl, maxLevel);

        return new DesignParameters(tr, factors, lcm, gl, sfMin);
    }

    /**
     * Valida el tamaño de fracción
     */
    public boolean validateFractionSize(int[] design, int fractionSize) {
        DesignParameters params = calculateParameters(design);
        return fractionSize >= params.sfMin() && fractionSize < params.tr();
    }

    /**
     * Genera fracciones aleatorias
     */
    public List<FractionResult> generateRandomFractions(int[] design, int fractionSize, int numberOfFractions) {
        validateInputs(design, fractionSize, numberOfFractions);

        DesignParameters params = calculateParameters(design);
        double[][] reflexMatrix = buildReflexMatrix(design, fractionSize);
        int[] randomStarts = MatlabFunctions.nonRepeatableRandomNumbers(1, params.tr(), numberOfFractions);

        return generateFractionsFromStarts(design, fractionSize, reflexMatrix, randomStarts);
    }

    /**
     * Genera fracciones personalizadas
     */
    public List<FractionResult> generateCustomFractions(int[] design, int fractionSize, List<Integer> customStarts) {
        if (!validateDesign(design)) {
            throw new IllegalArgumentException("Diseño no válido para NONBPA");
        }

        if (!validateFractionSize(design, fractionSize)) {
            throw new IllegalArgumentException("Tamaño de fracción no válido");
        }

        DesignParameters params = calculateParameters(design);
        validateCustomStarts(customStarts, params.tr());

        double[][] reflexMatrix = buildReflexMatrix(design, fractionSize);
        int[] customArray = customStarts.stream().mapToInt(Integer::intValue).toArray();

        return generateFractionsFromStarts(design, fractionSize, reflexMatrix, customArray);
    }

    private void validateInputs(int[] design, int fractionSize, int numberOfFractions) {
        if (!validateDesign(design)) {
            throw new IllegalArgumentException("Diseño no válido para NONBPA");
        }

        if (!validateFractionSize(design, fractionSize)) {
            throw new IllegalArgumentException("Tamaño de fracción no válido");
        }

        DesignParameters params = calculateParameters(design);
        if (numberOfFractions <= 0 || numberOfFractions > params.tr()) {
            throw new IllegalArgumentException("Número de fracciones no válido (1-" + params.tr() + ")");
        }
    }

    private void validateCustomStarts(List<Integer> customStarts, int maxValue) {
        for (int start : customStarts) {
            if (start < 1 || start > maxValue) {
                throw new IllegalArgumentException("Fracción " + start + " fuera del rango válido (1-" + maxValue + ")");
            }
        }
    }

    private double[][] buildReflexMatrix(int[] design, int fractionSize) {
        double[][] mainMatrix = generateMainEffectsMatrix(design);
        return createReflexMatrix(mainMatrix, fractionSize);
    }

    /**
     * Genera la matriz de efectos principales
     */
    private double[][] generateMainEffectsMatrix(int[] design) {
        int tr = calculateProduct(design);
        int factors = design.length;
        double[][] matrix = new double[tr][factors];

        for (int f = 0; f < factors; f++) {
            fillFactorColumn(matrix, f, design[f], tr);
        }

        return matrix;
    }

    private void fillFactorColumn(double[][] matrix, int col, int levels, int totalRows) {
        int blockSize = levels;
        int repeats = totalRows / levels;

        for (int repeat = 0; repeat < repeats; repeat++) {
            int startRow = repeat * blockSize;
            for (int level = 0; level < levels; level++) {
                matrix[startRow + level][col] = level + 1;
            }
        }
    }

    private double[][] createReflexMatrix(double[][] original, int fractionSize) {
        int originalRows = original.length;
        int factors = original[0].length;
        int reflexRows = fractionSize - 1;
        int totalRows = originalRows + reflexRows;

        double[][] reflexMatrix = new double[totalRows][factors];

        // Copiar matriz original
        for (int i = 0; i < originalRows; i++) {
            System.arraycopy(original[i], 0, reflexMatrix[i], 0, factors);
        }

        // Agregar reflejo
        for (int i = 0; i < reflexRows; i++) {
            System.arraycopy(original[i], 0, reflexMatrix[originalRows + i], 0, factors);
        }

        return reflexMatrix;
    }

    private List<FractionResult> generateFractionsFromStarts(int[] design, int fractionSize,
                                                             double[][] reflexMatrix, int[] starts) {
        List<FractionResult> results = new ArrayList<>(starts.length);
        int factors = design.length;

        for (int i = 0; i < starts.length; i++) {
            int startIndex = starts[i] - 1;

            // Extraer fracción de forma eficiente
            double[][] fraction = extractFraction(reflexMatrix, startIndex, fractionSize, factors);

            // Calcular métricas
            double gbm = gbmCalculator.calculateGBM(fraction, design);
            double j2 = j2Calculator.calculateJ2(fraction);
            double[] vifs = vifsCalculator.calculate(fraction);

            results.add(new FractionResult(i + 1, gbm, j2, vifs, fraction));
        }

        return results;
    }

    private double[][] extractFraction(double[][] source, int startRow, int rows, int cols) {
        double[][] fraction = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(source[startRow + i], 0, fraction[i], 0, cols);
        }

        return fraction;
    }

    private int calculateProduct(int[] array) {
        int product = 1;
        for (int value : array) {
            product *= value;
        }
        return product;
    }

    public record DesignParameters(int tr, int factors, int lcm, int gl, int sfMin) {}
}