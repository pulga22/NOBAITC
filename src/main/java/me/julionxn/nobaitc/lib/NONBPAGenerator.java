package me.julionxn.nobaitc.lib;

import java.util.*;
import java.util.stream.IntStream;

public class NONBPAGenerator {

    private final Scanner scanner;
    private int[] design;
    private int TR;       // Tamaño de la matriz del modelo
    private int factors;  // Número de factores
    private int LCM;      // Mínimo común múltiplo
    private int SF;       // Tamaño de la fracción

    /**
     * Constructor que inicializa el scanner para la entrada del usuario
     */
    public NONBPAGenerator() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Método principal que inicia el proceso de generación de fracciones
     */
    public void start() {
        showIntroduction();
        askDesign();

        // PASO UNO: Calcular TR y número de factores
        calculateInitParams();

        // Verificar errores iniciales
        if (!validateInitParams()) {
            return;
        }

        // PASO DOS: Definición de SF
        if (!defineFractionSize()) {
            return;
        }

        // PASO TRES: Generación de matriz de efectos principales
        double[][] matrixMeanEffectsNONBA = generateMatrixEffects();

        // Generación de fracciones
        generateFractions(matrixMeanEffectsNONBA);
    }

    /**
     * Muestra la introducción del programa
     */
    private void showIntroduction() {
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println("Genera fracciones para efectos principales de diseños factoriales de niveles mixtos puros");
        System.out.println("Considera únicamente aquellos diseños con capacidades para generar fracciones semi-balanceadas");
        System.out.println("semi-ortogonales de 2 a 9 factores");
        System.out.println("Los diseños de niveles mixtos puros tienen la característica de que el tamaño de la matriz");
        System.out.println("del modelo (TR) es igual al mínimo común múltiplo (LCM) de los niveles");
        System.out.println("------------------------------------------------------------------------------------------------");
    }

    /**
     * Solicita al usuario ingresar el diseño factorial
     */
    private void askDesign() {
        System.out.println("Para terminar ingrese \"x\"");
        String input = "";
        List<Integer> levels = new ArrayList<>();
        while (!input.equalsIgnoreCase("x")){
            System.out.print("Ingrese factor de la matriz del modelo (TR): ");
            input = scanner.nextLine().trim();
            Optional<Integer> value = parseInt(input);
            if (value.isEmpty()){
                System.out.println("Valor no válido");
                continue;
            }
            levels.add(value.get());
        }
        design = levels.stream().mapToInt(Integer::intValue).toArray();
    }

    private Optional<Integer> parseInt(String str){
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (NumberFormatException e){
            return Optional.empty();
        }
    }

    /**
     * Calcula parámetros iniciales: TR, factors y LCM
     */
    private void calculateInitParams() {
        // Calcular TR como el producto de todos los niveles
        TR = Arrays.stream(design).reduce(1, (a, b) -> a * b);

        // Número de factores
        factors = design.length;

        // Calcular LCM
        LCM = calculateLCM(design);

        System.out.println("Tamaño de la matriz del modelo (TR): " + TR);
        System.out.println("Número de factores: " + factors);
        System.out.println("Mínimo común múltiplo (LCM): " + LCM);
    }

    /**
     * Valida los parámetros iniciales del diseño
     * @return true si los parámetros son válidos, false en caso contrario
     */
    private boolean validateInitParams() {
        if (factors > 9 || TR != LCM) {
            System.out.println("ERROR 1: El diseño tiene un número superior a 9 factores y/o no pertenece a los Diseños de niveles mixtos puros");
            return false;
        }
        return true;
    }

    /**
     * Define el tamaño de la fracción (SF)
     * @return true si el SF es válido, false en caso contrario
     */
    private boolean defineFractionSize() {
        System.out.println("El diseño cuenta con grados de libertad (GL) necesarios para en el ANOVA conocer el efecto de cada factor, la intersección y el error");
        System.out.println("Se recomienda elegir un valor mayor o igual al tamaño de fracción mínimo (SFmin).");

        int GL = factors + 2;
        int maxLevel = Arrays.stream(design).max().getAsInt();
        int SFmin = Math.max(GL, maxLevel);

        System.out.println("GL = " + GL);
        System.out.println("maxLevel = " + maxLevel);
        System.out.println("SFmin = " + SFmin);

        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.print("Ingrese el tamaño de la fracción: ");
        SF = Integer.parseInt(scanner.nextLine().trim());

        if (SF < SFmin || SF >= TR) {
            System.out.println("ERROR 2: El valor asignado a SF no es válido");
            return false;
        }

        return true;
    }

    /**
     * Genera la matriz de efectos principales NONBA
     * @return matriz de efectos principales
     */
    private double[][] generateMatrixEffects() {
        double[][] matrixMeanEffectsNONBA = new double[TR][factors];

        for (int f = 0; f < factors; f++) {
            double[] columnsVector = new double[TR];
            double[] levelVector = IntStream.rangeClosed(1, design[f]).asDoubleStream().toArray();
            int timesRepeat = TR / design[f];

            for (int i = 0; i < timesRepeat; i++) {
                System.arraycopy(levelVector, 0, columnsVector, i * design[f], levelVector.length);
            }

            for (int i = 0; i < TR; i++) {
                matrixMeanEffectsNONBA[i][f] = columnsVector[i];
            }
        }

        return matrixMeanEffectsNONBA;
    }

    /**
     * Genera las fracciones NONBPA
     * @param matrixMeanEffectsNONBA matriz de efectos principales
     */
    private void generateFractions(double[][] matrixMeanEffectsNONBA) {
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println("Para este diseño es posible generar desde 1 hasta TR fracciones NONBPA");
        System.out.println("TR = " + TR);
        System.out.println("------------------------------------------------------------------------------------------------");

        System.out.print("Ingrese la cantidad de fracciones que desea generar: ");
        int fractionsSize = Integer.parseInt(scanner.nextLine().trim());

        if (fractionsSize > TR || fractionsSize == 0) {
            System.out.println("ERROR 3: El valor asignado al número de fracciones no es válido");
            return;
        }

        // Formar la matriz NONBA + reflejo
        double[][] nonbaReflexMatrix = new double[TR + SF - 1][factors];
        for (int i = 0; i < TR; i++) {
            System.arraycopy(matrixMeanEffectsNONBA[i], 0, nonbaReflexMatrix[i], 0, factors);
        }

        for (int i = 0; i < SF-1; i++) {
            System.arraycopy(matrixMeanEffectsNONBA[i], 0, nonbaReflexMatrix[TR + i], 0, factors);
        }

        // Tipo de fracciones (aleatorias o personalizadas)
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println("Desea que las fracciones sean --aleatorias teclee el valor 0 ó personalizadas --teclee el valor 1");
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.print("aleatorias(0)/personalizadas(1): ");

        int fractionsType = Integer.parseInt(scanner.nextLine().trim());
        int[] randomFractions;

        if (fractionsType == 0) {
            // Generar fracciones aleatorias
            randomFractions = nonRepeatableRandomNumbers(1, TR, fractionsSize);
            System.out.println("Fracciones aleatorias generadas: " + Arrays.toString(randomFractions));
        } else {
            // Fracciones personalizadas
            System.out.println("Ingrese las fracciones en el orden que desea que se presenten ej. Ingrese [1 2 3]");
            System.out.println("si desea generar la fracción uno seguida de la dos y tres.");
            System.out.println("Notar que el número de valores que introduzca debe ser igual al número de fracciones");
            System.out.print("Ingrese los números de las fracciones que desea generar de la forma [1 2 3]: ");

            String fractionsInput = scanner.nextLine().trim();
            fractionsInput = fractionsInput.replace("[", "").replace("]", "");
            String[] fraccionesStrings = fractionsInput.split(" ");
            randomFractions = new int[fraccionesStrings.length];

            for (int i = 0; i < fraccionesStrings.length; i++) {
                randomFractions[i] = Integer.parseInt(fraccionesStrings[i].trim());
            }
        }

        // Validar tamaño de randomFractions
        if (randomFractions.length != fractionsSize) {
            System.out.println("ERROR 5: La lista de fracciones a generar difiere del número de fracciones solicitadas");
            return;
        }

        // Generar cada una de las fracciones
        double[][] matrixGBMJ2 = new double[fractionsSize][2];

        BalancedGBMMatrix GBMcalculator = new BalancedGBMMatrix();
        OrthogonalJ2Matrix J2calculator = new OrthogonalJ2Matrix();

        for (int i = 0; i < fractionsSize; i++) {
            System.out.println("------------------------------------------------------------------------------------------------");

            int startingRun = randomFractions[i] - 1; // Ajustamos para índice base-0
            System.out.println("Corrida inicial de la fracción: " + (startingRun + 1));

            // Extraer la fracción NONBPA
            double[][] NONBPA = new double[SF][factors];
            for (int row = 0; row < SF; row++) {
                System.arraycopy(nonbaReflexMatrix[startingRun + row], 0, NONBPA[row], 0, factors);
            }

            // Mostrar la fracción
            System.out.println("Fracción NONBPA:");
            for (int fila = 0; fila < SF; fila++) {
                System.out.println(Arrays.toString(NONBPA[fila]));
            }

            // Calcular GBM y J2
            double GBM = GBMcalculator.calculateGBM(NONBPA, design);
            double J2 = J2calculator.calculateJ2(NONBPA);

            System.out.println("GBM = " + GBM);
            System.out.println("J2 = " + J2);

            // Guardar valores para distribuciones
            matrixGBMJ2[i][0] = GBM;
            matrixGBMJ2[i][1] = J2;

            System.out.println("------------------------------------------------------------------------------------------------");
        }

        // Mostrar resumen de las distribuciones
        System.out.println("\nResumen de las distribuciones GBM y J2:");
        System.out.println("Fracción\tGBM\t\tJ2");
        for (int i = 0; i < fractionsSize; i++) {
            System.out.printf("%d\t\t%.4f\t\t%.4f\n", i+1, matrixGBMJ2[i][0], matrixGBMJ2[i][1]);
        }
    }

    /**
     * Calcula el mínimo común múltiplo (LCM) de un arreglo de enteros
     * @param numbers arreglo de enteros
     * @return mínimo común múltiplo
     */
    private int calculateLCM(int[] numbers) {
        int result = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            result = lcm(result, numbers[i]);
        }
        return result;
    }

    /**
     * Calcula el mínimo común múltiplo (LCM) de dos números
     * @param a primer número
     * @param b segundo número
     * @return mínimo común múltiplo
     */
    private int lcm(int a, int b) {
        return (a * b) / gcd(a, b);
    }

    /**
     * Calcula el máximo común divisor (GCD) mediante el algoritmo de Euclides
     * @param a primer número
     * @param b segundo número
     * @return máximo común divisor
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
     * Genera un arreglo de números aleatorios sin repetición
     * @param min valor mínimo
     * @param max valor máximo
     * @param count cantidad de números a generar
     * @return arreglo de números aleatorios sin repetición
     */
    private int[] nonRepeatableRandomNumbers(int min, int max, int count) {
        if (max - min + 1 < count) {
            throw new IllegalArgumentException("No se pueden generar " + count + " números únicos en el rango especificado");
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

}
