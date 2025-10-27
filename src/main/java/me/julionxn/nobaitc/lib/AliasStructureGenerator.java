package me.julionxn.nobaitc.lib;

import me.julionxn.nobaitc.util.FormatHelper;

import java.util.*;

/**
 * Generador de estructura de alias para diseños factoriales fraccionados
 * Traducción y optimización del código MATLAB original
 */
public class AliasStructureGenerator {

    private final double[][] array;
    private final int m; // filas
    private final int n; // columnas (factores)
    private double ponderacion = 0.5;

    // Variables del proceso
    private double[][] matrizCorrelaciones;
    private double[][] T; // Triangular inferior
    private double VL; // Valor límite
    private double[][] W; // Matriz de correlaciones absolutas
    private int A; // Total de efectos
    private int L; // Total de efectos
    private int me; // Número de efectos principales
    private int doble; // Número de interacciones de 2 factores
    private int triple; // Número de interacciones de 3 factores

    // Matrices de letras generadas dinámicamente
    private String[] renglonLetras;
    private String[][] matrixLetras;

    // Resultado final
    private double[][] MSZ;
    private AliasStructure aliasStructure;

    /**
     * Constructor principal
     * @param fraction Matriz del diseño factorial fraccionado
     */
    public AliasStructureGenerator(double[][] fraction) {
        if (fraction == null || fraction.length == 0) {
            throw new IllegalArgumentException("La fracción no puede estar vacía");
        }
        this.array = fraction;
        this.m = fraction.length;
        this.n = fraction[0].length;
    }

    /**
     * Genera la estructura de alias
     * @return Estructura de alias calculada
     */
    public AliasStructure generate() {
        // PASO 1-3: Calcular correlaciones
        matrizCorrelaciones = calcularCorrelaciones();
        // PASO 4: Procesar matriz de correlaciones
        paso4();

        // Verificar correlaciones fuertes entre efectos principales
        if (verificarCorrelacionesFuertes()) {
            System.out.println("La fracción contiene efectos principales que están fuertemente correlacionados (r>0.5)");
            return null;
        }

        // PASO 5: Calcular alias
        paso5();

        // Generar estructura de alias
        aliasStructure = new AliasStructure(MSZ, renglonLetras, matrixLetras, me);

        return aliasStructure;
    }

    /**
     * PASO 1-3: Calcula la matriz de correlaciones
     */
    private double[][] calcularCorrelaciones() {
        // Generar las combinaciones de letras
        generarCombinacionesLetras();

        // Construir matriz del modelo con todas las interacciones
        double[][] matrizModelo = construirMatrizModelo();

        // Calcular matriz de correlaciones
        return MatlabFunctions.corrcoef(matrizModelo);
    }

    /**
     * Construye la matriz del modelo con todas las columnas de efectos e interacciones
     */
    private double[][] construirMatrizModelo() {
        List<double[]> columnas = new ArrayList<>();

        // Calcular máximos por columna para normalizar
        int[] maximos = new int[n];
        for (int j = 0; j < n; j++) {
            int max = (int) array[0][j];
            for (int i = 1; i < m; i++) {
                if (array[i][j] > max) {
                    max = (int) array[i][j];
                }
            }
            maximos[j] = max;
        }

        // Normalizar columnas principales (efectos principales)
        double[][] columnasNormalizadas = new double[n][m];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                columnasNormalizadas[j][i] = 1 - ((2.0 * (maximos[j] - array[i][j])) / (maximos[j] - 1));
            }
            columnas.add(columnasNormalizadas[j]);
        }

        // Generar interacciones de 2 factores
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double[] interaccion = multiplicarColumnas(columnasNormalizadas[i], columnasNormalizadas[j]);
                columnas.add(interaccion);
            }
        }

        // Generar interacciones de 3 factores
        if (n > 2) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    for (int k = j + 1; k < n; k++) {
                        double[] int2 = multiplicarColumnas(columnasNormalizadas[i], columnasNormalizadas[j]);
                        double[] interaccion = multiplicarColumnas(int2, columnasNormalizadas[k]);
                        columnas.add(interaccion);
                    }
                }
            }
        }

        // Convertir lista de columnas a matriz
        double[][] resultado = new double[m][columnas.size()];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < columnas.size(); j++) {
                resultado[i][j] = columnas.get(j)[i];
            }
        }

        return resultado;
    }

    /**
     * Multiplica elemento a elemento dos columnas
     */
    private double[] multiplicarColumnas(double[] col1, double[] col2) {
        double[] resultado = new double[col1.length];
        for (int i = 0; i < col1.length; i++) {
            resultado[i] = col1[i] * col2[i];
        }
        return resultado;
    }

    /**
     * Genera las combinaciones de letras dinámicamente
     */
    private void generarCombinacionesLetras() {
        char[] letras = "ABCDEFGHJ".toCharArray(); // Nota: usa J en lugar de I
        List<String> combinaciones = new ArrayList<>();

        // Efectos principales
        for (int i = 0; i < n; i++) {
            combinaciones.add(String.valueOf(letras[i]));
        }

        // Interacciones de 2 factores
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                combinaciones.add("" + letras[i] + letras[j]);
            }
        }

        // Interacciones de 3 factores
        if (n > 2) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    for (int k = j + 1; k < n; k++) {
                        combinaciones.add("" + letras[i] + letras[j] + letras[k]);
                    }
                }
            }
        }

        // Convertir a array
        renglonLetras = combinaciones.toArray(new String[0]);
        L = renglonLetras.length;

        // Generar matrixLetras (repeticiones)
        int numRepeticiones = L;
        matrixLetras = new String[L][numRepeticiones];
        for (int i = 0; i < L; i++) {
            Arrays.fill(matrixLetras[i], renglonLetras[i]);
        }
    }

    /**
     * PASO 4: Procesa la matriz de correlaciones
     */
    private void paso4() {
        // Triangular inferior
        T = MatlabFunctions.tril(matrizCorrelaciones);

        // Encontrar máximo absoluto (excluyendo diagonal)
        double maxCorr = 0;
        for (int i = 0; i < T.length; i++) {
            for (int j = 0; j < T[i].length; j++) {
                if (i != j && Math.abs(T[i][j]) > maxCorr) {
                    maxCorr = Math.abs(T[i][j]);
                }
            }
        }

        VL = maxCorr * ponderacion;

        // Matriz de valores absolutos
        W = new double[T.length][T[0].length];
        for (int i = 0; i < T.length; i++) {
            for (int j = 0; j < T[i].length; j++) {
                W[i][j] = Math.abs(T[i][j]);
                // Redondear valores muy pequeños a cero
                if (W[i][j] < 0.0001) {
                    W[i][j] = 0;
                }
            }
        }

        A = W.length;
        L = W[0].length;

        // Calcular me, doble, triple
        me = n;
        doble = MatlabFunctions.nchoosek(n, 2);
        triple = n > 2 ? MatlabFunctions.nchoosek(n, 3) : 0;
    }

    /**
     * Verifica si hay correlaciones fuertes entre efectos principales
     */
    private boolean verificarCorrelacionesFuertes() {
        for (int col = 0; col < me - 1; col++) {
            for (int fila = col + 1; fila < me; fila++) {
                if (Math.abs(W[fila][col]) >= 1.5) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * PASO 5: Calcula la estructura de alias
     */
    private void paso5() {
        // Buscar correlaciones superiores al VL
        double[][] revW = buscarCorrelacionesSuperioresAlVL();

        // Verificar si hay alias
        int sumaVectorAlias = contarAlias(revW);

        if (sumaVectorAlias == 0) {
            // Diseño ortogonal
            MSZ = new double[L][L];
            for (int i = 0; i < L; i++) {
                MSZ[i][i] = 1;
            }
            System.out.println("ALIAS CALCULADOS CORRECTAMENTE, DISEÑO ORTOGONAL");
        } else {
            // Localizar y asignar correlaciones
            double[][] D = localizarCorrelacionesSuperioresAlVL(revW);
            FormatHelper.printMatrix(D);
            double[][] CH = asignarCorrelacionesInferioresAlVL(D);
            MSZ = cambioDeSignos(CH);
        }
    }

    /**
     * Busca correlaciones superiores al valor límite
     */
    private double[][] buscarCorrelacionesSuperioresAlVL() {
        double[][] revW = new double[A][L];

        for (int v = 0; v < A; v++) {
            for (int i = 0; i < L; i++) {
                double valor = W[v][i];
                // Eliminar unos de la diagonal y valores menores a VL
                if (i == v || Math.abs(valor - 1.0) < 0.0001) {
                    revW[v][i] = 0;
                } else if (valor < VL) {
                    revW[v][i] = 0;
                } else {
                    revW[v][i] = valor;
                }
            }
        }

        return revW;
    }

    /**
     * Cuenta el número de alias presentes
     */
    private int contarAlias(double[][] matriz) {
        int suma = 0;
        for (double[] doubles : matriz) {
            boolean tieneAlias = false;
            for (double aDouble : doubles) {
                if (aDouble > 0) {
                    tieneAlias = true;
                    break;
                }
            }
            if (tieneAlias) suma++;
        }
        return suma;
    }

    /**
     * Localiza correlaciones superiores al VL y determina alias principales
     */
    private double[][] localizarCorrelacionesSuperioresAlVL(double[][] eM) {
        // Clasificar renglones según donde tienen correlaciones
        int[] vecceros = new int[L];

        for (int vv = 0; vv < A; vv++) {
            double maxMe = MatlabFunctions.maxInRange(eM[vv], 0, me);
            double maxDoble = MatlabFunctions.maxInRange(eM[vv], me, me + doble);
            double maxTotal = MatlabFunctions.max(eM[vv]);

            if (maxTotal == 0) {
                vecceros[vv] = 0;
            } else if (maxMe != 0) {
                vecceros[vv] = 1;
            } else if (maxDoble != 0) {
                vecceros[vv] = 2;
            }
        }

        // Procesar renglones tipo 1 (correlacionados con efectos principales)
        for (int fx = me; fx < A; fx++) {
            if (vecceros[fx] == 1) {
                analizarRenglonDeMe(eM, fx);
            }
        }

        // Procesar renglones tipo 2 (correlacionados con interacciones de 2 factores)
        for (int vx = me; vx < A; vx++) {
            if (vecceros[vx] == 2) {
                analizarRenglonesDe2FI(eM, vx);
            }
        }

        // Reclasificar para interacciones de 3 factores
        if (triple > 0) {
            for (int gk = me + doble; gk < A; gk++) {
                double maxMeDoble = MatlabFunctions.maxInRange(eM[gk], 0, me + doble);
                double max3 = MatlabFunctions.maxInRange(eM[gk], me + doble, L);
                if (maxMeDoble == 0 && max3 != 0) {
                    vecceros[gk] = 3;
                }
            }

            // Procesar renglones tipo 3
            for (int vxx = me + doble; vxx < A; vxx++) {
                if (vecceros[vxx] == 3) {
                    analizarRenglonesDe3FI(eM, vxx);
                }
            }
        }

        return eM;
    }

    /**
     * Analiza renglón correlacionado con efectos principales
     */
    private void analizarRenglonDeMe(double[][] eM, int fila) {
        double[] renglon = eM[fila].clone();

        // Solo considerar la parte de efectos principales
        double[] vctr = Arrays.copyOfRange(renglon, 0, me);

        // Encontrar el máximo
        int maxIdx = MatlabFunctions.argmax(vctr);
        double maxVal = vctr[maxIdx];

        if (maxVal != 0) {
            // Poner en cero todos los valores menores al máximo
            for (int c = 0; c < me; c++) {
                if (vctr[maxIdx] > vctr[c]) {
                    vctr[c] = 0;
                } else if (Math.abs(vctr[maxIdx] - vctr[c]) < 0.0001 && maxIdx != c) {
                    vctr[c] = 0;
                }
            }

            // Actualizar renglón
            System.arraycopy(vctr, 0, eM[fila], 0, me);

            // Poner en cero la columna correspondiente
            for (int i = 0; i < A; i++) {
                eM[i][fila] = 0;
            }
        }
    }

    /**
     * Analiza renglón correlacionado con interacciones de 2 factores
     */
    private void analizarRenglonesDe2FI(double[][] eM, int fila) {
        double[] vctr = Arrays.copyOfRange(eM[fila], me, me + doble);

        double maxVal = MatlabFunctions.max(vctr);
        if (maxVal == 0) return;

        int maxIdx = MatlabFunctions.argmax(vctr);

        // Limpiar el renglón
        for (int c = 0; c < vctr.length; c++) {
            if (vctr[maxIdx] > vctr[c]) {
                vctr[c] = 0;
            } else if (Math.abs(vctr[maxIdx] - vctr[c]) < 0.0001 && maxIdx != c) {
                vctr[c] = 0;
            }
        }

        // Actualizar
        System.arraycopy(vctr, 0, eM[fila], me, doble);

        for (int i = 0; i < A; i++) {
            eM[i][fila] = 0;
        }
    }

    /**
     * Analiza renglón correlacionado con interacciones de 3 factores
     */
    private void analizarRenglonesDe3FI(double[][] eM, int fila) {
        double[] vctr = Arrays.copyOfRange(eM[fila], me + doble, L);

        double maxVal = MatlabFunctions.max(vctr);
        if (maxVal == 0) return;

        int maxIdx = MatlabFunctions.argmax(vctr);

        for (int c = 0; c < vctr.length; c++) {
            if (vctr[maxIdx] > vctr[c]) {
                vctr[c] = 0;
            } else if (Math.abs(vctr[maxIdx] - vctr[c]) < 0.0001 && maxIdx != c) {
                vctr[c] = 0;
            }
        }

        System.arraycopy(vctr, 0, eM[fila], me + doble, triple);

        for (int i = 0; i < A; i++) {
            eM[i][fila] = 0;
        }
    }

    /**
     * Asigna correlaciones inferiores al VL
     */
    private double[][] asignarCorrelacionesInferioresAlVL(double[][] D) {
        // UD: Matriz sin unos en la diagonal
        double[][] UD = new double[A][L];
        for (int dd = 0; dd < A; dd++) {
            for (int i = 0; i < L; i++) {
                if (i == dd && Math.abs(W[dd][i] - 1.0) < 0.0001) {
                    UD[dd][i] = 0;
                } else {
                    UD[dd][i] = W[dd][i];
                }
            }
        }

        // MFL: Asignar valores
        double[][] MFL = new double[A][L];
        for (int f = 0; f < A; f++) {
            double maxRenglon = MatlabFunctions.max(D[f]);
            if (f < me || maxRenglon == 0) {
                System.arraycopy(UD[f], 0, MFL[f], 0, L);
            } else {
                System.arraycopy(D[f], 0, MFL[f], 0, L);
            }
        }

        // ML: Limpiar columnas con valores
        double[][] ML = new double[A][L];
        for (int ff = 0; ff < L; ff++) {
            System.arraycopy(MFL[ff], 0, ML[ff], 0, L);
            if (ff >= me) {
                double maxCol = MatlabFunctions.maxCol(D, ff);
                if (maxCol != 0) {
                    Arrays.fill(ML[ff], 0);
                }
            }
        }

        // MZ: Proceso iterativo de asignación
        double[][] MZ = new double[A][L];
        int[] vecAyuda = new int[L];

        for (int ll = 0; ll < A; ll++) {
            System.arraycopy(ML[ll], 0, MZ[ll], 0, L);

            boolean tieneValor = false;
            for (int i = 0; i < me; i++) {
                if (D[ll][i] > 0) {
                    tieneValor = true;
                    break;
                }
            }
            vecAyuda[ll] = tieneValor ? 1 : 0;
        }

        // Proceso de limpieza
        for (int ee = me; ee < A; ee++) {
            if (vecAyuda[ee] == 0) {
                for (int uu = me; uu < L; uu++) {
                    if (MZ[ee][uu] != 0) {
                        if (vecAyuda[uu] == 1) {
                            MZ[ee][uu] = 0;
                        } else {
                            vecAyuda[ee] = 1;
                        }
                    }
                }
            }
        }

        // MX: Seleccionar máximos por renglón
        double[][] MX = new double[A][L];
        for (int fff = 0; fff < A; fff++) {
            double[] renglon = MZ[fff].clone();
            double maxVal = MatlabFunctions.max(renglon);

            if (maxVal != 0) {
                int maxIdx = MatlabFunctions.argmax(renglon);
                for (int rr = 0; rr < L; rr++) {
                    if (renglon[maxIdx] > renglon[rr]) {
                        renglon[rr] = 0;
                    } else if (Math.abs(renglon[maxIdx] - renglon[rr]) < 0.0001 && maxIdx != rr) {
                        renglon[rr] = 0;
                    }
                }
            }
            System.arraycopy(renglon, 0, MX[fff], 0, L);
        }

        // CH: Limpiar columnas y agregar diagonal
        double[][] CH = new double[L][L];
        for (int eee = 0; eee < L; eee++) {
            System.arraycopy(MX[eee], 0, CH[eee], 0, L);
            if (eee >= me) {
                double maxCol = MatlabFunctions.maxCol(MX, eee);
                if (maxCol != 0) {
                    Arrays.fill(CH[eee], 0);
                }
            }
        }

        // Agregar diagonal para efectos principales
        for (int ss = 0; ss < me; ss++) {
            CH[ss][ss] = 1;
        }

        // Agregar diagonal para efectos con alias
        for (int sss = me; sss < L; sss++) {
            double maxCol = MatlabFunctions.maxCol(CH, sss);
            if (maxCol != 0) {
                CH[sss][sss] = 1;
            }
        }

        // Verificar que todos tengan alias
        int numeroAlias = 0;
        for (int y = 0; y < L; y++) {
            boolean tieneAlias = false;
            for (int i = 0; i < L; i++) {
                if (CH[y][i] > 0) {
                    tieneAlias = true;
                    break;
                }
            }
            if (tieneAlias) numeroAlias++;
        }

        // Completar alias faltantes
        if (numeroAlias < L) {
            for (int yy = me; yy < L; yy++) {
                double maxRen = MatlabFunctions.max(CH[yy]);
                if (maxRen == 0) {
                    CH[yy][yy] = 1;
                }
            }
        }

        System.out.println("ALIAS CALCULADOS CORRECTAMENTE");
        return CH;
    }

    /**
     * Cambio de signos según la matriz T original
     */
    private double[][] cambioDeSignos(double[][] CH) {
        double[][] MSZ = new double[A][L];

        for (int ppp = 0; ppp < A; ppp++) {
            for (int i = 0; i < L; i++) {
                if (Math.abs(T[ppp][i]) == Math.abs(CH[ppp][i])) {
                    MSZ[ppp][i] = T[ppp][i];
                } else {
                    MSZ[ppp][i] = 0;
                }
            }
        }

        return MSZ;
    }

    // ==================== CLASE INTERNA: AliasStructure ====================

    /**
     * Representa la estructura de alias resultante
     */
    public static class AliasStructure {
        private final double[][] matrizAlias;
        private final String[] efectos;
        private final String[][] matrizLetras;
        private final int numEfectosPrincipales;
        private final Map<String, List<AliasPair>> aliasMap;

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
         * Imprime la estructura de alias
         */
        public void print() {
            System.out.println("\n============ ESTRUCTURA DE ALIAS ============");

            for (String efecto : efectos) {
                List<AliasPair> alias = aliasMap.get(efecto);
                if (alias != null && !alias.isEmpty()) {
                    System.out.println("\n============");
                    System.out.println("  EFECTO");
                    System.out.println("  " + efecto);
                    System.out.println("     =");

                    for (AliasPair par : alias) {
                        System.out.printf(" %+f\n", par.coeficiente);
                        System.out.println("  " + par.efecto);
                    }
                }
            }

            System.out.println("\n============================================");
        }

        // Getters
        public double[][] getMatrizAlias() { return matrizAlias; }
        public String[] getEfectos() { return efectos; }
        public Map<String, List<AliasPair>> getAliasMap() { return aliasMap; }
        public int getNumEfectosPrincipales() { return numEfectosPrincipales; }
    }

    /**
     * Representa un par de alias (coeficiente + efecto)
     */
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

    // ==================== GETTERS ====================

    public double[][] getMatrizCorrelaciones() { return matrizCorrelaciones; }
    public double[][] getMSZ() { return MSZ; }
    public String[] getRenglonLetras() { return renglonLetras; }
    public AliasStructure getAliasStructure() { return aliasStructure; }
}