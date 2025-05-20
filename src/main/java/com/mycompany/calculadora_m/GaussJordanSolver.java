package com.mycompany.calculadora_m;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GaussJordanSolver {

    public static class Resultado {
        public List<String> pasos;
        public String[] soluciones;
        public List<String> solucionesSimbolicas;
        public String tipoSolucion;

        public Resultado(List<String> pasos, String[] soluciones, 
                        List<String> solucionesSimbolicas, String tipoSolucion) {
            this.pasos = pasos;
            this.soluciones = soluciones;
            this.solucionesSimbolicas = solucionesSimbolicas;
            this.tipoSolucion = tipoSolucion;
        }
    }

    public static Resultado resolver(String[][] matrizStr) {
        double[][] matriz = parseMatriz(matrizStr);
        return resolverInterno(matriz);
    }

    public static Resultado resolver(double[][] matrizDouble) {
        String[][] matrizStr = new String[matrizDouble.length][matrizDouble[0].length];
        for (int i = 0; i < matrizDouble.length; i++) {
            for (int j = 0; j < matrizDouble[i].length; j++) {
                matrizStr[i][j] = formatDouble(matrizDouble[i][j]);
            }
        }
        return resolver(matrizStr);
    }

    private static Resultado resolverInterno(double[][] matriz) {
        int filas = matriz.length;
        int columnas = matriz[0].length;
        List<String> pasos = new ArrayList<>();
        
        pasos.add("Matriz inicial:");
        pasos.add(matrizToString(matriz));

        // Fase de eliminación
        int rank = 0;
        for (int col = 0; col < columnas - 1 && rank < filas; col++) {
            int filaPivote = encontrarPivote(matriz, rank, col, filas);
            if (filaPivote == -1) continue;
            
            if (filaPivote != rank) {
                intercambiarFilas(matriz, rank, filaPivote);
                pasos.add("Intercambio fila " + (rank+1) + " y " + (filaPivote+1));
                pasos.add(matrizToString(matriz));
            }
            
            double pivote = matriz[rank][col];
            if (!esCero(pivote)) {
                for (int j = col; j < columnas; j++) {
                    matriz[rank][j] /= pivote;
                }
                pasos.add("Normalizar fila " + (rank+1) + " con pivote " + formatDouble(pivote));
                pasos.add(matrizToString(matriz));
            }
            
            for (int i = 0; i < filas; i++) {
                if (i != rank && !esCero(matriz[i][col])) {
                    double factor = matriz[i][col];
                    for (int j = col; j < columnas; j++) {
                        matriz[i][j] -= factor * matriz[rank][j];
                    }
                    pasos.add("Eliminar en fila " + (i+1) + " usando fila " + (rank+1));
                    pasos.add(matrizToString(matriz));
                }
            }
            rank++;
        }

        return analizarSolucion(matriz, pasos, filas, columnas);
    }

    private static Resultado analizarSolucion(double[][] matriz, List<String> pasos, int filas, int columnas) {
        boolean inconsistente = false;
        int numVariables = columnas - 1;
        int rank = 0;
        
        for (int i = 0; i < filas; i++) {
            if (!esFilaCero(matriz[i], columnas - 1)) rank++;
            else if (!esCero(matriz[i][columnas - 1])) inconsistente = true;
        }
        
        if (inconsistente) {
            return new Resultado(pasos, null, null, "Sistema incompatible: no tiene solución");
        } else if (rank < numVariables) {
            List<String> solucionesParam = obtenerSolucionesParametricas(matriz, filas, columnas);
            return new Resultado(pasos, null, solucionesParam, 
                              "Sistema con infinitas soluciones (" + (numVariables - rank) + " variables libres)");
        } else {
            String[] soluciones = new String[numVariables];
            for (int i = 0; i < numVariables; i++) {
                soluciones[i] = convertirAFraccionExacta(matriz[i][columnas - 1]);
            }
            return new Resultado(pasos, soluciones, null, "Sistema con solución única");
        }
    }

    private static List<String> obtenerSolucionesParametricas(double[][] matriz, int filas, int columnas) {
        List<String> soluciones = new ArrayList<>();
        int numVariables = columnas - 1;
        boolean[] esVariableLibre = new boolean[numVariables];
        
        // Identificar variables libres
        for (int j = 0; j < numVariables; j++) {
            esVariableLibre[j] = true;
            for (int i = 0; i < filas; i++) {
                if (Math.abs(matriz[i][j] - 1) < 1e-8) {
                    boolean esPivote = true;
                    for (int k = 0; k < j; k++) {
                        if (!esCero(matriz[i][k])) {
                            esPivote = false;
                            break;
                        }
                    }
                    if (esPivote) esVariableLibre[j] = false;
                }
            }
        }

        // Construir soluciones
        for (int j = 0; j < numVariables; j++) {
            StringBuilder sb = new StringBuilder("x").append(j+1).append(" = ");
            
            if (!esVariableLibre[j]) {
                boolean primerTermino = true;
                
                for (int i = 0; i < filas; i++) {
                    if (Math.abs(matriz[i][j] - 1) < 1e-8) {
                        // Término independiente
                        if (!esCero(matriz[i][numVariables])) {
                            sb.append(convertirAFraccionExacta(matriz[i][numVariables]));
                            primerTermino = false;
                        }
                        
                        // Variables libres
                        for (int k = 0; k < numVariables; k++) {
                            if (esVariableLibre[k] && !esCero(matriz[i][k])) {
                                double coef = -matriz[i][k];
                                String coefStr = convertirAFraccionExacta(coef);
                                
                                if (!primerTermino) {
                                    sb.append(coef > 0 ? " + " : " - ");
                                    sb.append(coefStr.replace("-", ""));
                                } else {
                                    sb.append(coefStr);
                                }
                                sb.append("*x").append(k+1);
                                primerTermino = false;
                            }
                        }
                        break;
                    }
                }
                
                if (primerTermino) sb.append("0");
            } else {
                sb.append("x").append(j+1);
            }
            
            soluciones.add(sb.toString());
        }
        
        return soluciones;
    }

    // Métodos auxiliares
    private static String convertirAFraccionExacta(double valor) {
        if (esCero(valor)) return "0";
        
        for (int den = 1; den <= 20; den++) {
            for (int num = -20; num <= 20; num++) {
                if (num != 0 && Math.abs(valor - (double)num/den) < 1e-8) {
                    return (den == 1) ? Integer.toString(num) : num + "/" + den;
                }
            }
        }
        return String.format("%.4f", valor).replaceAll("\\.?0+$", "");
    }

    private static double[][] parseMatriz(String[][] matrizStr) {
        double[][] matriz = new double[matrizStr.length][matrizStr[0].length];
        for (int i = 0; i < matrizStr.length; i++) {
            for (int j = 0; j < matrizStr[i].length; j++) {
                matriz[i][j] = parseFraction(matrizStr[i][j]);
            }
        }
        return matriz;
    }

    private static double parseFraction(String str) {
        if (str.contains("/")) {
            String[] parts = str.split("/");
            return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
        }
        return Double.parseDouble(str);
    }

    private static String formatDouble(double value) {
        return String.format("%.4f", value).replaceAll("\\.?0+$", "");
    }

    private static String matrizToString(double[][] matriz) {
        StringBuilder sb = new StringBuilder();
        for (double[] fila : matriz) {
            for (double val : fila) {
                sb.append(String.format("%9.4f", val)).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static int encontrarPivote(double[][] matriz, int filaInicio, int col, int filas) {
        for (int i = filaInicio; i < filas; i++) {
            if (!esCero(matriz[i][col])) return i;
        }
        return -1;
    }

    private static boolean esFilaCero(double[] fila, int numColumnas) {
        for (int j = 0; j < numColumnas; j++) {
            if (!esCero(fila[j])) return false;
        }
        return true;
    }

    private static boolean esCero(double value) {
        return Math.abs(value) < 1e-8;
    }

    private static void intercambiarFilas(double[][] matriz, int fila1, int fila2) {
        double[] temp = matriz[fila1];
        matriz[fila1] = matriz[fila2];
        matriz[fila2] = temp;
    }
}