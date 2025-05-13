package me.julionxn.nobaitc.lib;

public class OrthogonalJ2Matrix {

    /**
     * Calcula el parámetro J2 para una fracción dada
     * @param fraccion matriz que representa la fracción
     * @return valor del parámetro J2
     */
    public double calculateJ2(double[][] fraccion) {
        int SF = fraccion.length;         // Número de filas (tamaño de la fracción)
        int factors = fraccion[0].length; // Número de columnas (factores)

        // Matriz que alberga los valores cuadráticos calculados para el parámetro J2
        double[][] matJ2 = new double[SF][SF-1];

        for (int row = 0; row < SF-1; row++) {
            // Obtiene el primer renglón
            double[] line = fraccion[row];

            for (int row2 = row+1; row2 < SF; row2++) {
                // Renglones siguientes para comparar
                double[] nextLines = fraccion[row2];
                int[] comparatorVector = new int[factors];

                // Comparar cada uno de los elementos del renglón
                for (int factor = 0; factor < factors; factor++) {
                    if (line[factor] == nextLines[factor]) {
                        comparatorVector[factor] = 1;
                    } else {
                        comparatorVector[factor] = 0;
                    }
                }

                // Elevar al cuadrado la sum del vector de comparación
                int sum = 0;
                for (int value : comparatorVector) {
                    sum += value;
                }
                double s1 = Math.pow(sum, 2);

                // Enviar los valores a la matriz global
                matJ2[row2][row] = s1;
            }
        }

        // Calcular la suma total de J2
        double ORTHOGONALJ2 = 0;
        for (int i = 0; i < SF; i++) {
            for (int j = 0; j < SF-1; j++) {
                ORTHOGONALJ2 += matJ2[i][j];
            }
        }

        return ORTHOGONALJ2;
    }

}
