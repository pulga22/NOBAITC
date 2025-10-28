package me.julionxn.nobaitc.util;

import java.util.Arrays;

public class FormatHelper {

    private static final String[] letters = new String[]{
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
    };

    public static String getLetter(int index){
        return letters[index % letters.length];
    }

    /**
     * Formatea un vector (double[])
     */
    public static String formatVector(double[] vector) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        int maxElements = Math.min(5, vector.length);
        for (int i = 0; i < maxElements; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.4f", vector[i]));
        }

        if (vector.length > maxElements) {
            sb.append(", ...");
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Formatea una matriz (double[][]) de forma recursiva
     */
    public static String formatMatrix(double[][] matrix) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        int maxRows = Math.min(3, matrix.length);
        for (int i = 0; i < maxRows; i++) {
            if (i > 0) sb.append("; ");
            sb.append(formatVector(matrix[i]));
        }

        if (matrix.length > maxRows) {
            sb.append("; ...]");
        } else {
            sb.append("]");
        }

        return sb.toString();
    }

    public static void printMatrix(double[][] matrix) {
        StringBuilder sb = new StringBuilder("[");
        for (double[] row : matrix) {
            sb.append("[");
            for (double value : row) {
                sb.append(String.format("%.4f", value)).append(", ");
                //sb.append(value).append(", ");
            }
            sb.append("],").append("\n");
        }
        sb.append("]");
        System.out.println(sb);
    }

    public static void printMatrix(double[] matrix){
        StringBuilder sb = new StringBuilder();
        sb.append("[").append("\n");
        for (double value : matrix) {
            sb.append(value).append(", ");
        }
        sb.append("]");
        System.out.println(sb);
    }

    public static void printMatrix(int[] matrix){
        StringBuilder sb = new StringBuilder();
        sb.append("[").append("\n");
        for (double value : matrix) {
            sb.append(value).append(", \n");
        }
        sb.append("]");
        System.out.println(sb);
    }

}
