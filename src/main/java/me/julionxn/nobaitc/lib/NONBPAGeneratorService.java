package me.julionxn.nobaitc.lib;

import me.julionxn.nobaitc.models.FractionResult;

import java.util.*;
import java.util.stream.IntStream;

public class NONBPAGeneratorService {

    private final BalancedGBMMatrix gbmCalculator;
    private final OrthogonalJ2Matrix j2Calculator;

    public NONBPAGeneratorService() {
        this.gbmCalculator = new BalancedGBMMatrix();
        this.j2Calculator = new OrthogonalJ2Matrix();
    }

    /**
     * Valida si un diseño es válido para NONBPA
     */
    public boolean validateDesign(int[] design) {
        if (design.length > 9) return false;

        int tr = Arrays.stream(design).reduce(1, (a, b) -> a * b);
        int lcm = calculateLCM(design);

        return tr == lcm;
    }

    /**
     * Calcula parámetros del diseño
     */
    public DesignParameters calculateParameters(int[] design) {
        int tr = Arrays.stream(design).reduce(1, (a, b) -> a * b);
        int factors = design.length;
        int lcm = calculateLCM(design);
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
        return fractionSize >= params.sfMin && fractionSize < params.tr;
    }

    /**
     * Genera fracciones aleatorias
     */
    public List<FractionResult> generateRandomFractions(int[] design, int fractionSize, int numberOfFractions) {
        if (!validateDesign(design)) {
            throw new IllegalArgumentException("Diseño no válido para NONBPA");
        }

        if (!validateFractionSize(design, fractionSize)) {
            throw new IllegalArgumentException("Tamaño de fracción no válido");
        }

        DesignParameters params = calculateParameters(design);
        if (numberOfFractions > params.tr || numberOfFractions <= 0) {
            throw new IllegalArgumentException("Número de fracciones no válido");
        }

        // Generar matriz de efectos principales
        double[][] matrixEffects = generateMatrixEffects(design);

        // Generar matriz NONBA + reflejo
        double[][] nonbaReflexMatrix = createReflexMatrix(matrixEffects, fractionSize);

        // Generar números aleatorios sin repetición
        int[] randomStarts = generateRandomNumbers(1, params.tr, numberOfFractions);

        return generateFractionsFromStarts(design, fractionSize, nonbaReflexMatrix, randomStarts);
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

        // Validar fracciones personalizadas
        for (int start : customStarts) {
            if (start < 1 || start > params.tr) {
                throw new IllegalArgumentException("Fracción " + start + " fuera del rango válido (1-" + params.tr + ")");
            }
        }

        // Generar matriz de efectos principales
        double[][] matrixEffects = generateMatrixEffects(design);

        // Generar matriz NONBA + reflejo
        double[][] nonbaReflexMatrix = createReflexMatrix(matrixEffects, fractionSize);

        // Convertir a array para compatibilidad
        int[] customArray = customStarts.stream().mapToInt(Integer::intValue).toArray();

        return generateFractionsFromStarts(design, fractionSize, nonbaReflexMatrix, customArray);
    }

    /**
     * Genera la matriz de efectos principales
     */
    private double[][] generateMatrixEffects(int[] design) {
        int tr = Arrays.stream(design).reduce(1, (a, b) -> a * b);
        int factors = design.length;
        double[][] matrix = new double[tr][factors];

        for (int f = 0; f < factors; f++) {
            double[] columnsVector = new double[tr];
            double[] levelVector = IntStream.rangeClosed(1, design[f]).asDoubleStream().toArray();
            int timesRepeat = tr / design[f];

            for (int i = 0; i < timesRepeat; i++) {
                System.arraycopy(levelVector, 0, columnsVector, i * design[f], levelVector.length);
            }

            for (int i = 0; i < tr; i++) {
                matrix[i][f] = columnsVector[i];
            }
        }

        return matrix;
    }

    /**
     * Crea la matriz con reflejo
     */
    private double[][] createReflexMatrix(double[][] originalMatrix, int fractionSize) {
        int tr = originalMatrix.length;
        int factors = originalMatrix[0].length;
        double[][] reflexMatrix = new double[tr + fractionSize - 1][factors];

        // Copiar matriz original
        for (int i = 0; i < tr; i++) {
            System.arraycopy(originalMatrix[i], 0, reflexMatrix[i], 0, factors);
        }

        // Agregar reflejo
        for (int i = 0; i < fractionSize - 1; i++) {
            System.arraycopy(originalMatrix[i], 0, reflexMatrix[tr + i], 0, factors);
        }

        return reflexMatrix;
    }

    /**
     * Genera fracciones a partir de posiciones iniciales
     */
    private List<FractionResult> generateFractionsFromStarts(int[] design, int fractionSize,
                                                             double[][] reflexMatrix, int[] starts) {
        List<FractionResult> results = new ArrayList<>();
        int factors = design.length;

        for (int i = 0; i < starts.length; i++) {
            int startIndex = starts[i] - 1; // Ajustar a índice base-0

            // Extraer fracción
            double[][] fraction = new double[fractionSize][factors];
            for (int row = 0; row < fractionSize; row++) {
                System.arraycopy(reflexMatrix[startIndex + row], 0, fraction[row], 0, factors);
            }

            // Calcular métricas
            double gbm = gbmCalculator.calculateGBM(fraction, design);
            double j2 = j2Calculator.calculateJ2(fraction);

            // Convertir fracción a string para mostrar
            String fractionData = formatFraction(fraction);

            results.add(new FractionResult(i + 1, fractionData, gbm, j2, fraction));
        }

        return results;
    }

    /**
     * Formatea una fracción para mostrar en la tabla
     */
    private String formatFraction(double[][] fraction) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < Math.min(3, fraction.length); i++) { // Mostrar solo las primeras 3 filas
            if (i > 0) sb.append("; ");
            sb.append(Arrays.toString(fraction[i]));
        }
        if (fraction.length > 3) {
            sb.append("; ...]");
        } else {
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * Calcula el LCM de un array
     */
    public int calculateLCM(int[] numbers) {
        int result = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            result = lcm(result, numbers[i]);
        }
        return result;
    }

    /**
     * Calcula LCM de dos números
     */
    private int lcm(int a, int b) {
        return (a * b) / gcd(a, b);
    }

    /**
     * Calcula GCD usando algoritmo de Euclides
     */
    private int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Genera números aleatorios sin repetición
     */
    private int[] generateRandomNumbers(int min, int max, int count) {
        if (max - min + 1 < count) {
            throw new IllegalArgumentException("No se pueden generar " + count + " números únicos en el rango");
        }

        int[] result = new int[count];
        boolean[] used = new boolean[max - min + 1];
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int num;
            do {
                num = random.nextInt(max - min + 1) + min;
            } while (used[num - min]);

            used[num - min] = true;
            result[i] = num;
        }

        return result;
    }

    /**
         * Clase para encapsular parámetros del diseño
         */
        public record DesignParameters(int tr, int factors, int lcm, int gl, int sfMin) {
    }
}