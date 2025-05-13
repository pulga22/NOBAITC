package me.julionxn.nobaitc.lib;

import java.util.Arrays;

public class BalancedGBMMatrix {

    /**
     * Calcula el parámetro GBM para una fracción dada
     * @param fraction matriz que representa la fracción
     * @param design arreglo con los niveles de cada factor
     * @return valor del parámetro GBM
     */
    public double calculateGBM(double[][] fraction, int[] design) {
        int SF = fraction.length;         // Número de filas (tamaño de la fracción)
        int factors = fraction[0].length; // Número de columnas (factores)

        // Matriz que contendrá el número de veces que se repite cada nivel
        double[][] matrixFindings = new double[Arrays.stream(design).max().getAsInt()][factors];

        // Contar el número de veces que se repite cada nivel en cada factor
        for (int factor = 0; factor < factors; factor++) {
            // Vector que conforma cada factor
            double[] fractionVector = new double[SF];
            for (int i = 0; i < SF; i++) {
                fractionVector[i] = fraction[i][factor];
            }

            // Generar vector de valores de búsqueda
            for (int level = 1; level <= design[factor]; level++) {
                int contador = 0;

                // Contar ocurrencias del level
                for (int i = 0; i < SF; i++) {
                    if (fractionVector[i] == level) {
                        contador++;
                    }
                }

                matrixFindings[level-1][factor] = contador;
            }
        }

        // Calcular el número de veces que debería aparecer cada nivel para cada factor
        double[] matrixShould = new double[factors];
        for (int factor = 0; factor < factors; factor++) {
            matrixShould[factor] = (double)SF / design[factor];
        }

        // Cálculo de diferencias de cuadrados
        double[][] squareDifs = new double[Arrays.stream(design).max().getAsInt()][factors];
        for (int factor = 0; factor < factors; factor++) {
            double shouldCount = matrixShould[factor];

            for (int level = 0; level < design[factor]; level++) {
                squareDifs[level][factor] =
                        Math.pow(matrixFindings[level][factor] - shouldCount, 2);
            }
        }

        // Calcular GBM por factor
        double[] GBMMatrixFraction = new double[factors];
        for (int factor = 0; factor < factors; factor++) {
            double sum = 0;
            for (int level = 0; level < design[factor]; level++) {
                sum += squareDifs[level][factor];
            }
            GBMMatrixFraction[factor] = sum;
        }

        // Calcular GBM total
        double balancedGBM = 0;
        for (int factor = 0; factor < factors; factor++) {
            balancedGBM += GBMMatrixFraction[factor];
        }

        return balancedGBM;
    }

}
