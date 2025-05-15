package com.mycompany.calculadora_m;

import java.util.ArrayList;
import java.util.List;

public class GaussJordanSolver {

    public static class Resultado {
        public List<String> pasos;
        public double[] soluciones;                // para soluciones unicas
        public List<String> solucionesSimbolicas;  // para infinitas soluciones (parametricas)
        public String tipoSolucion;

        public Resultado(List<String> pasos, double[] soluciones, List<String> solucionesSimbolicas, String tipoSolucion) {
            this.pasos = pasos;
            this.soluciones = soluciones;
            this.solucionesSimbolicas = solucionesSimbolicas;
            this.tipoSolucion = tipoSolucion;
        }
    }
    public static Resultado resolver(double[][] matrizOriginal) {
        int filas = matrizOriginal.length;
        int columnas = matrizOriginal[0].length;
        List<String> pasos = new ArrayList<>();
        double[][] matriz = new double[filas][columnas];
        for (int i = 0; i < filas; i++) {
            System.arraycopy(matrizOriginal[i], 0, matriz[i], 0, columnas);
        }
        pasos.add("Matriz inicial:");
        pasos.add(matrizToString(matriz));

        // Gauss-Jordan
        for (int i = 0; i < filas; i++) {
            double pivote = matriz[i][i];
            if (Math.abs(pivote) < 1e-8) {
                pasos.add("No se puede continuar: pivote cero en fila " + (i + 1));
                continue;
            }
            if (pivote != 1.0) {
                pasos.add(String.format("Multiplicamos fila %d por 1/%.3f para hacer el pivote igual a 1", i + 1, pivote));
                for (int j = 0; j < columnas; j++) {
                    matriz[i][j] /= pivote;
                }
                pasos.add(matrizToString(matriz));
            }
            for (int k = 0; k < filas; k++) {
                if (k != i) {
                    double factor = matriz[k][i];
                    if (Math.abs(factor) > 1e-8) {
                        String operacion = (factor > 0)
                                ? String.format("Restamos %.3f * fila %d a fila %d", factor, i + 1, k + 1)
                                : String.format("Sumamos %.3f * fila %d a fila %d", -factor, i + 1, k + 1);
                        pasos.add(operacion);
                        for (int j = 0; j < columnas; j++) {
                            matriz[k][j] -= factor * matriz[i][j];
                        }
                        pasos.add(matrizToString(matriz));
                    }
                }
            }
        }

        // Verificación de tipos de solución
        String tipoSolucion;
        double[] soluciones = null;
        List<String> solucionesSimbolicas = null;
        boolean inconsistente = false;
        boolean infinitasSoluciones = false;

        for (int i = 0; i < filas; i++) {
            boolean filaCero = true;
            for (int j = 0; j < columnas - 1; j++) {
                if (Math.abs(matriz[i][j]) > 1e-8) {
                    filaCero = false;
                    break;
                }
            }
            if (filaCero && Math.abs(matriz[i][columnas - 1]) > 1e-8) {
                inconsistente = true;
                break;
            } else if (filaCero) {
                infinitasSoluciones = true;
            }
        }

        if (inconsistente) {
            tipoSolucion = "El sistema no tiene solución.";
        } else if (infinitasSoluciones) {
            tipoSolucion = "El sistema tiene infinitas soluciones.";

            solucionesSimbolicas = new ArrayList<>();

            int numVariables = columnas - 1;
            boolean[] esPivote = new boolean[numVariables]; 
            for (int i = 0; i < filas; i++) {
                for (int j = 0; j < numVariables; j++) {
                    if (Math.abs(matriz[i][j] - 1) < 1e-8) {
                        boolean esPivoteFila = true;
                        for (int c = 0; c < numVariables; c++) {
                            if (c != j && Math.abs(matriz[i][c]) > 1e-8) {
                                esPivoteFila = false;
                                break;
                            }
                        }
                        if (esPivoteFila) {
                            esPivote[j] = true;
                            break;
                        }
                    }
                }
            }
            int parametroCount = 1;
            String[] expresiones = new String[numVariables];
            for (int i = 0; i < numVariables; i++) {
                if (!esPivote[i]) {
                    expresiones[i] = "t" + parametroCount;
                    parametroCount++;
                }
            }

            for (int i = 0; i < filas; i++) {
                int pivoteCol = -1;
                for (int j = 0; j < numVariables; j++) {
                    if (Math.abs(matriz[i][j] - 1) < 1e-8) {
                        pivoteCol = j;
                        break;
                    }
                }
                if (pivoteCol == -1) continue;

                StringBuilder expr = new StringBuilder();
                expr.append(String.format("x%d = ", pivoteCol + 1));
                expr.append(String.format("%.3f", matriz[i][columnas - 1]));

                for (int j = 0; j < numVariables; j++) {
                    if (j != pivoteCol && !esPivote[j]) {
                        double coef = -matriz[i][j];
                        if (Math.abs(coef) > 1e-8) {
                            if (coef > 0) expr.append(" + ");
                            else expr.append(" - ");
                            expr.append(String.format("%.3f", Math.abs(coef))).append("*t");
                            int idxParam = 1;
                            for (int k = 0; k < j; k++) {
                                if (!esPivote[k]) idxParam++;
                            }
                            expr.append(idxParam);
                        }
                    }
                }
                expresiones[pivoteCol] = expr.toString();
            }

            for (int i = 0; i < numVariables; i++) {
                if (!esPivote[i]) {
                    expresiones[i] = "x" + (i + 1) + " = " + expresiones[i];
                }
            }

            for (String s : expresiones) {
                solucionesSimbolicas.add(s);
            }

        } else {
            tipoSolucion = "El sistema tiene una única solución.";
            soluciones = new double[filas];
            for (int i = 0; i < filas; i++) {
                soluciones[i] = matriz[i][columnas - 1];
            }
        }

        return new Resultado(pasos, soluciones, solucionesSimbolicas, tipoSolucion);
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
}