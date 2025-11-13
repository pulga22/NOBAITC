package me.julionxn.nobaitc.data.alias;

import me.julionxn.nobaitc.data.MatlabFunctions;
import me.julionxn.nobaitc.util.FormatHelper;

import java.util.*;

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

    private String[] renglonLetras;
    private String[][] matrixLetras;

    // Resultado final
    private double[][] MSZ;

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
        return new AliasStructure(MSZ, renglonLetras, matrixLetras, me);
    }

    /**
     * PASO 1-3
     */
    private double[][] calcularCorrelaciones() {
        // Generar las combinaciones de letras
        generarCombinacionesLetras();

        // Construir matriz del modelo con todas las interacciones
        double[][] matrizModelo = construirMatrizModelo();

        // Calcular matriz de correlaciones
        return MatlabFunctions.corrcoef(matrizModelo);
    }

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

        double[][] columnasNormalizadas = new double[n][m];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                columnasNormalizadas[j][i] = 1 - ((2.0 * (maximos[j] - array[i][j])) / (maximos[j] - 1));
            }
            columnas.add(columnasNormalizadas[j]);
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double[] interaccion = multiplicarColumnas(columnasNormalizadas[i], columnasNormalizadas[j]);
                columnas.add(interaccion);
            }
        }

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

        for (int i = 0; i < n; i++) {
            combinaciones.add(String.valueOf(letras[i]));
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                combinaciones.add("" + letras[i] + letras[j]);
            }
        }

        if (n > 2) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    for (int k = j + 1; k < n; k++) {
                        combinaciones.add("" + letras[i] + letras[j] + letras[k]);
                    }
                }
            }
        }

        renglonLetras = combinaciones.toArray(new String[0]);
        L = renglonLetras.length;

        int numRepeticiones = L;
        matrixLetras = new String[L][numRepeticiones];
        for (int i = 0; i < L; i++) {
            Arrays.fill(matrixLetras[i], renglonLetras[i]);
        }
    }

    /**
     * PASO 4
     */
    private void paso4() {
        T = MatlabFunctions.tril(matrizCorrelaciones);

        double maxCorr = 0;
        for (int i = 0; i < T.length; i++) {
            for (int j = 0; j < T[i].length; j++) {
                if (i != j && Math.abs(T[i][j]) > maxCorr) {
                    maxCorr = Math.abs(T[i][j]);
                }
            }
        }

        VL = maxCorr * ponderacion;

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
     * PASO 5
     */
    private void paso5() {
        double[][] revW = buscarCorrelacionesSuperioresAlVL();

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
            double[][] CH = asignarCorrelacionesInferioresAlVL(D);
            MSZ = cambioDeSignos(CH);
        }
    }

    /**
     * Busca correlaciones superiores
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

    private double[][] localizarCorrelacionesSuperioresAlVL(double[][] eM) {
        System.out.println("________________EM_0_______________");
        FormatHelper.printMatrix(eM);
        System.out.println("ME: " + me);

        int[] vecceros = new int[L];

        for (int vv = 0; vv < A; vv++) {
            double fencuentra = MatlabFunctions.maxInRange(eM[vv], 0, me);
            double ffencuentra = MatlabFunctions.maxInRange(eM[vv], me, me + doble);
            double fffencuentra = MatlabFunctions.max(eM[vv]);

            if (fffencuentra == 0) {
                vecceros[vv] = 0;
            } else if (fencuentra != 0) {
                vecceros[vv] = 1;
            } else if (ffencuentra != 0) {
                vecceros[vv] = 2;
            }
        }

        for (int fx = me; fx < A; fx++) {
            if (vecceros[fx] == 1) {
                double[] rengexamin1 = eM[fx].clone();
                double[] vctr = Arrays.copyOfRange(rengexamin1, 0, me);

                int h = MatlabFunctions.argmax(vctr);
                double r = vctr[h];

                if (r != 0) {
                    // Limpiar vctr
                    for (int c = 0; c < me; c++) {
                        if (vctr[h] > vctr[c]) {
                            vctr[c] = 0;
                        } else if (Math.abs(vctr[h] - vctr[c]) < 0.0001) {
                            if (h != c) {
                                vctr[c] = 0;
                            }
                        }
                    }

                    System.arraycopy(vctr, 0, eM[fx], 0, me);

                    // Poner ceros en la segunda parte
                    //todo: chechar esto
                    for (int i = me; i < L; i++) {
                        eM[fx][i] = 0;
                    }

                    // Poner ceros en toda la columna fx
                    //todo: checar esto
                    for (int i = 0; i < A; i++) {
                        eM[i][fx] = 0;
                    }
                }
            }
        }

        for (int vx = me; vx < A; vx++) {
            if (vecceros[vx] == 2) {
                double[] rengloncoaexaminar = eM[vx].clone();

                double[] rangoDobles = Arrays.copyOfRange(rengloncoaexaminar, me, me + doble);
                double vc = MatlabFunctions.max(rangoDobles);

                if (vc != 0) {
                    double[] vctrr = Arrays.copyOfRange(rengloncoaexaminar, me, me + doble);
                    int hg = MatlabFunctions.argmax(vctrr);
                    double rg = vctrr[hg];

                    if (rg != 0) {
                        // Limpiar vctrr
                        for (int cc = 0; cc < doble; cc++) {
                            if (vctrr[hg] > vctrr[cc]) {
                                vctrr[cc] = 0;
                            } else if (Math.abs(vctrr[hg] - vctrr[cc]) < 0.0001) {
                                if (hg != cc) {
                                    vctrr[cc] = 0;
                                }
                            }
                        }

                        //todo: checar
                        Arrays.fill(eM[vx], 0);

                        System.arraycopy(vctrr, 0, eM[vx], me, doble);

                        for (int i = 0; i < A; i++) {
                            eM[i][vx] = 0;
                        }
                    }
                }
            }
        }

        if (triple > 0) {
            for (int gk = me + doble; gk < A; gk++) {
                double ffffencuentra = MatlabFunctions.maxInRange(eM[gk], 0, me + doble);
                double fff3encuentra = MatlabFunctions.maxInRange(eM[gk], me + doble, L);

                if (ffffencuentra == 0 && fff3encuentra != 0) {
                    vecceros[gk] = 3;
                }
            }
            for (int vxx = me + doble; vxx < A; vxx++) {
                if (vecceros[vxx] == 3) {
                    double[] rengloncoaexaminartres = eM[vxx].clone();

                    double ir = MatlabFunctions.max(rengloncoaexaminartres);

                    if (ir != 0) {
                        double[] vctrrr = Arrays.copyOfRange(rengloncoaexaminartres, me + doble, L);

                        int hgg = MatlabFunctions.argmax(vctrrr);
                        double rgg = vctrrr[hgg];

                        if (rgg != 0) {
                            for (int ccc = 0; ccc < triple; ccc++) {
                                if (vctrrr[hgg] > vctrrr[ccc]) {
                                    vctrrr[ccc] = 0;
                                } else if (Math.abs(vctrrr[hgg] - vctrrr[ccc]) < 0.0001) {
                                    if (hgg != ccc) {
                                        vctrrr[ccc] = 0;
                                    }
                                }
                            }

                            System.arraycopy(vctrrr, 0, eM[vxx], me + doble, triple);

                            // Poner ceros en toda la columna vxx
                            for (int i = 0; i < A; i++) {
                                eM[i][vxx] = 0;
                            }
                        }
                    }
                }
            }
        }

        return eM;
    }

    /**
     * Asigna correlaciones inferiores al VL
     */
    private double[][] asignarCorrelacionesInferioresAlVL(double[][] D) {

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

        double[][] MFL = new double[A][L];
        for (int f = me; f < A; f++) {
            double maxRenglon = MatlabFunctions.max(D[f]);
            if (f < me || maxRenglon == 0) {
                System.arraycopy(UD[f], 0, MFL[f], 0, L);
            } else {
                System.arraycopy(D[f], 0, MFL[f], 0, L);
            }
        }

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

        for (int ss = 0; ss < me; ss++) {
            CH[ss][ss] = 1;
        }

        for (int sss = me; sss < L; sss++) {
            double maxCol = MatlabFunctions.maxCol(CH, sss);
            if (maxCol != 0) {
                CH[sss][sss] = 1;
            }
        }

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
}