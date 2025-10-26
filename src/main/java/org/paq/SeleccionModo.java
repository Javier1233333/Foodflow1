package org.paq;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SeleccionModo extends JFrame {

    // Colores
    private static final Color VERDE = new Color(24, 102, 24);
    private static final Color FONDO = Color.WHITE;

    public SeleccionModo() {
        // Ventana
        setTitle("Selecciona el modo");
        setSize(960, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Layout absoluto
        JPanel root = (JPanel) getContentPane();
        root.setLayout(null);
        root.setBackground(FONDO);

        // ---- Logo arriba-derecha ----
        JLabel logo = new JLabel();
        logo.setHorizontalAlignment(SwingConstants.RIGHT);
        loadLogoOrFallback(logo);
        logo.setBounds(630, 16, 334, 113);
        root.add(logo);

        // ---- Título alineado a la izquierda ----
        JLabel titulo = new JLabel("<html>Selecciona el<br>modo</html>");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 32));
        titulo.setForeground(new Color(40, 40, 40));
        titulo.setBounds(120, 80, 400, 90);
        root.add(titulo);

        // ---- Botón Empleado ----
        JButton btnEmpleado = new JButton("Empleado");
        btnEmpleado.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnEmpleado.setForeground(Color.WHITE);
        btnEmpleado.setBackground(VERDE);
        btnEmpleado.setFocusPainted(false);
        btnEmpleado.setBorderPainted(false);
        btnEmpleado.setOpaque(true);
        btnEmpleado.setBounds(200, 250, 220, 120);

        // **************************************************
        // CAMBIO: Llama a FoodFlowLogin (Login de Empleado)
        // **************************************************
        btnEmpleado.addActionListener(e -> {
            dispose();
            new FoodFlowLogin().setVisible(true); // O DashboardEmpleado si el login ya fue hecho
        });
        root.add(btnEmpleado);

        // ---- Botón Administrador ----
        JButton btnAdmin = new JButton("Administrador");
        btnAdmin.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnAdmin.setForeground(Color.WHITE);
        btnAdmin.setBackground(VERDE);
        btnAdmin.setFocusPainted(false);
        btnAdmin.setBorderPainted(false);
        btnAdmin.setOpaque(true);
        btnAdmin.setBounds(540, 250, 220, 120);

        // **************************************************
        // CAMBIO: Llama a AdminLogin
        // **************************************************
        btnAdmin.addActionListener(e -> {
            dispose();
            new AdminLogin().setVisible(true);
        });
        root.add(btnAdmin);
    }

    private void loadLogoOrFallback(JLabel target) {
        try {
            URL img = SeleccionModo.class.getResource("/ima/Food1.png");
            if (img != null) {
                target.setIcon(new ImageIcon(img));
            } else {
                target.setText("FoodFlow");
                target.setFont(new Font("SansSerif", Font.BOLD, 26));
                target.setForeground(new Color(25, 25, 25));
            }
        } catch (Exception e) {
            target.setText("FoodFlow");
        }
    }

    // Nota: Eliminé la clase VentanaPrototipo ya que usarás las clases reales.

    // Main para iniciar la aplicación
    public static void main(String[] args) {
        // Debes inicializar el Login de Empleado para que esta clase compile
        // Si FoodFlowLogin no existe, usa un placeholder.
        SwingUtilities.invokeLater(() -> new SeleccionModo().setVisible(true));
    }
}
