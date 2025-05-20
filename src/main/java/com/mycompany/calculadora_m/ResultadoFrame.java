package com.mycompany.calculadora_m;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.util.List;

public class ResultadoFrame extends JFrame {
    // Constructor para matriz de doubles (mantiene compatibilidad)
    public ResultadoFrame(double[][] matriz) {
        super("Resultado del Sistema por Gauss-Jordan");
        configurarFrame();
        GaussJordanSolver.Resultado resultado = GaussJordanSolver.resolver(matriz);
        inicializarComponentes(resultado);
    }

    // Nuevo constructor para matriz de Strings (acepta fracciones)
    public ResultadoFrame(String[][] matrizStr) {
        super("Resultado del Sistema por Gauss-Jordan");
        configurarFrame();
        GaussJordanSolver.Resultado resultado = GaussJordanSolver.resolver(matrizStr);
        inicializarComponentes(resultado);
    }

    private void configurarFrame() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes(GaussJordanSolver.Resultado resultado) {
        JPanel panelContenido = new JPanel(new BorderLayout(10, 10));
        panelContenido.setBackground(Color.WHITE);
        panelContenido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titulo = new JLabel("Pasos de resolución con Gauss-Jordan", JLabel.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(new Color(33, 64, 128));
        panelContenido.add(titulo, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBackground(new Color(230, 230, 230));
        textArea.setForeground(Color.DARK_GRAY);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(generarTextoResultado(resultado));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panelContenido.add(scroll, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBackground(new Color(33, 64, 128));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCerrar.setPreferredSize(new Dimension(100, 35));
        btnCerrar.addActionListener(e -> dispose());

        JPanel panelBoton = new JPanel();
        panelBoton.setBackground(Color.WHITE);
        panelBoton.add(btnCerrar);
        panelContenido.add(panelBoton, BorderLayout.SOUTH);

        add(panelContenido, BorderLayout.CENTER);
        setVisible(true);
    }

    private String generarTextoResultado(GaussJordanSolver.Resultado resultado) {
        StringBuilder texto = new StringBuilder();

        // Mostrar pasos
        for (String paso : resultado.pasos) {
            texto.append(paso).append("\n");
        }

        // Mostrar resultados
        texto.append("\n--- Resultado final ---\n")
              .append(resultado.tipoSolucion).append("\n");

        if (resultado.soluciones != null) {
            texto.append("\n* Soluciones únicas *\n");
            for (int i = 0; i < resultado.soluciones.length; i++) {
                texto.append("x").append(i + 1).append(" = ")
                      .append(resultado.soluciones[i]).append("\n");
            }
        } else if (resultado.solucionesSimbolicas != null) {
            texto.append("\n* Soluciones generales (infinitas soluciones) *\n");
            for (String s : resultado.solucionesSimbolicas) {
                texto.append(s).append("\n");
            }
        }
        return texto.toString();
    }
}