package me.julionxn.nobaitc.data.alias;

import lombok.Getter;

import java.util.*;

/**
 * Representa la estructura de alias resultante del análisis.
 * Contiene la matriz de alias y métodos de utilidad para consultar información.
 */
@Getter
public class AliasStructure {

    private final double[][] matrizAlias;
    private final String[] efectos;
    private final String[][] matrizLetras;
    private final int numEfectosPrincipales;
    private final Map<String, List<AliasPair>> aliasMap;

    /**
     * Constructor
     */
    public AliasStructure(double[][] MSZ, String[] renglonLetras, String[][] matrixLetras, int me) {
        this.matrizAlias = MSZ;
        this.efectos = renglonLetras;
        this.matrizLetras = matrixLetras;
        this.numEfectosPrincipales = me;
        this.aliasMap = new HashMap<>();
        construirMapaAlias();
    }

    /**
     * Construye el mapa de alias para fácil acceso
     */
    private void construirMapaAlias() {
        for (int x = 0; x < efectos.length; x++) {
            List<AliasPair> pares = new ArrayList<>();

            for (int xx = 0; xx < matrizAlias.length; xx++) {
                if (matrizAlias[xx][x] != 0) {
                    pares.add(new AliasPair(matrizAlias[xx][x], matrizLetras[xx][x]));
                }
            }

            if (!pares.isEmpty()) {
                aliasMap.put(efectos[x], pares);
            }
        }
    }

    /**
     * Imprime la estructura de alias en consola
     */
    public void print() {
        System.out.println("\n============ ESTRUCTURA DE ALIAS ============");

        for (String efecto : efectos) {
            List<AliasPair> alias = aliasMap.get(efecto);
            if (alias != null && !alias.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append(" |>| Efecto: ").append(efecto).append(" = ");
                for (AliasPair par : alias) {
                    sb.append(" ").append(par.coeficiente).append(" ").append(par.efecto).append(" + ");
                }
                System.out.println(sb);
            }
        }

        System.out.println("\n============================================");
    }

    /**
     * Verifica si el diseño es ortogonal
     * (todos los efectos solo están aliados consigo mismos)
     */
    public boolean isOrthogonal() {
        for (List<AliasPair> pairs : aliasMap.values()) {
            if (pairs.size() > 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cuenta el número de efectos que tienen alias
     */
    public int getAliasCount() {
        int count = 0;
        for (List<AliasPair> pairs : aliasMap.values()) {
            if (pairs.size() > 1) {
                count++;
            }
        }
        return count;
    }

    /**
     * Representa un par de alias (coeficiente + efecto)
     */
    @Getter
    public static class AliasPair {
        public double coeficiente;
        public String efecto;

        public AliasPair(double coeficiente, String efecto) {
            this.coeficiente = coeficiente;
            this.efecto = efecto;
        }

        @Override
        public String toString() {
            return String.format("%+.4f %s", coeficiente, efecto);
        }
    }
}