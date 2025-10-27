package me.julionxn.nobaitc.lib;

import java.util.Random;

public class MatlabFunctions {

    /**
     * Calcula el mínimo común múltiplo (LCM) de un arreglo de enteros
     * @param numbers arreglo de enteros
     * @return mínimo común múltiplo
     */
    public static int calculateLCM(int[] numbers) {
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
    public static int lcm(int a, int b) {
        return (a * b) / gcd(a, b);
    }

    /**
     * Calcula el máximo común divisor (GCD) mediante el algoritmo de Euclides
     * @param a primer número
     * @param b segundo número
     * @return máximo común divisor
     */
    public static int gcd(int a, int b) {
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
    public static int[] nonRepeatableRandomNumbers(int min, int max, int count) {
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

    /**
     * Calcula la matriz de coeficientes de correlación
     * Equivalente a corrcoef(X) en MATLAB
     *
     * @param matrix matriz de datos (filas = observaciones, columnas = variables)
     * @return matriz de correlación simétrica
     */
    public static double[][] corrcoef(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            throw new IllegalArgumentException("La matriz no puede estar vacía");
        }

        int rows = matrix.length;
        int cols = matrix[0].length;

        // Transponer para trabajar con columnas como variables
        double[][] transposed = transpose(matrix);

        // Calcular matriz de correlación
        double[][] corrMatrix = new double[cols][cols];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == j) {
                    corrMatrix[i][j] = 1.0; // Diagonal es 1
                } else if (j < i) {
                    corrMatrix[i][j] = corrMatrix[j][i]; // Simetría
                } else {
                    corrMatrix[i][j] = pearsonCorrelation(transposed[i], transposed[j]);
                }
            }
        }

        return corrMatrix;
    }

    /**
     * Calcula corrcoef entre dos vectores
     * Equivalente a corrcoef(x, y) en MATLAB
     */
    public static double[][] corrcoef(double[] x, double[] y) {
        double[][] matrix = new double[x.length][2];
        for (int i = 0; i < x.length; i++) {
            matrix[i][0] = x[i];
            matrix[i][1] = y[i];
        }
        return corrcoef(matrix);
    }

    /**
     * Calcula el coeficiente de correlación de Pearson entre dos arrays
     */
    public static double pearsonCorrelation(double[] x, double[] y) {
        if (x.length != y.length || x.length == 0) {
            throw new IllegalArgumentException("Arrays inválidos");
        }
        int n = x.length;
        double meanX = mean(x);
        double meanY = mean(y);
        double numerator = 0.0;
        double sumSqX = 0.0;
        double sumSqY = 0.0;
        for (int i = 0; i < n; i++) {
            double diffX = x[i] - meanX;
            double diffY = y[i] - meanY;

            numerator += diffX * diffY;
            sumSqX += diffX * diffX;
            sumSqY += diffY * diffY;
        }
        double denominator = Math.sqrt(sumSqX * sumSqY);
        if (denominator == 0) {
            return Double.NaN;
        }
        return numerator / denominator;
    }

    /**
     * Calcula la media de un array
     */
    public static double mean(double[] array) {
        double sum = 0.0;
        for (double value : array) {
            sum += value;
        }
        return sum / array.length;
    }

    /**
     * Transpone una matriz
     */
    public static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }

        return transposed;
    }

    /**
     * Calcula la matriz inversa usando eliminación de Gauss-Jordan
     * Equivalente a inv() de MATLAB
     * @param matrix Matriz cuadrada a invertir
     * @return Matriz inversa o null si la matriz es singular
     */
    public static double[][] inv(double[][] matrix) {
        int n = matrix.length;

        // Verificar que sea matriz cuadrada
        for (double[] doubles : matrix) {
            if (doubles.length != n) {
                throw new IllegalArgumentException("La matriz debe ser cuadrada");
            }
        }

        double[][] augmented = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, n);
            augmented[i][n + i] = 1.0;
        }

        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(augmented[k][i]) > Math.abs(augmented[maxRow][i])) {
                    maxRow = k;
                }
            }
            double[] temp = augmented[i];
            augmented[i] = augmented[maxRow];
            augmented[maxRow] = temp;

            if (Math.abs(augmented[i][i]) < 1e-10) {
                return null;
            }

            double pivot = augmented[i][i];
            for (int j = 0; j < 2 * n; j++) {
                augmented[i][j] /= pivot;
            }

            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmented[k][i];
                    for (int j = 0; j < 2 * n; j++) {
                        augmented[k][j] -= factor * augmented[i][j];
                    }
                }
            }
        }

        double[][] inverse = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(augmented[i], n, inverse[i], 0, n);
        }

        return inverse;
    }

    /**
     * Extrae la diagonal de una matriz o crea una matriz diagonal
     * Equivalente a diag() de MATLAB
     * @param vector Vector o matriz
     * @return Matriz diagonal o vector diagonal
     */
    public static double[][] diag(double[] vector) {
        int n = vector.length;
        double[][] result = new double[n][n];

        for (int i = 0; i < n; i++) {
            result[i][i] = vector[i];
        }

        return result;
    }

    /**
     * Extrae la diagonal principal de una matriz
     * @param matrix Matriz de entrada
     * @return Vector con los elementos de la diagonal
     */
    public static double[] diag(double[][] matrix) {
        int n = Math.min(matrix.length, matrix[0].length);
        double[] result = new double[n];

        for (int i = 0; i < n; i++) {
            result[i] = matrix[i][i];
        }

        return result;
    }

}
