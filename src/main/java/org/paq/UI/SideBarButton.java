package org.paq.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SideBarButton extends JButton {

    private boolean selected = false;
    private boolean hovering = false;

    // Colores para los estados
    private final Color ACTIVE_COLOR = new Color(255, 255, 255, 50);
    private final Color HOVER_COLOR = new Color(255, 255, 255, 15);
    private final Color PRESSED_COLOR = new Color(255, 255, 255, 30);

    // Asumiendo que existe UIConstants y BASE (o similar)
    private final Font FONT = UIConstants.BASE.deriveFont(15f).deriveFont(Font.BOLD);

    // Constructor que acepta el texto y un icono
    public SideBarButton(String text, ImageIcon icon) {
        super(text);

        // --- Configuración Esencial ---
        setFocusPainted(false);
        setBorderPainted(false);
        setFocusable(false);

        // --- Configuración de Estilo ---
        setHorizontalAlignment(LEFT);
        setHorizontalTextPosition(RIGHT);
        setIconTextGap(10);

        setForeground(Color.WHITE);
        setFont(FONT);
        setIcon(icon);

        setOpaque(false);
        setContentAreaFilled(false);

        setBorder(new EmptyBorder(12, 10, 12, 10));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // --- Listener para Hover ---
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!selected) {
                    hovering = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }
        });
    }

    public void setSelectedItem(boolean sel) {
        selected = sel;
        if (selected) {
            hovering = false;
        }
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    // 🔑 CORRECCIÓN FINAL: Sobrescribir paintBorder
    @Override
    protected void paintBorder(Graphics g) {
        // No dibujar nada. Esto anula completamente el borde residual del L&F.
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 1. Dibujar el fondo del estado seleccionado
        if (selected) {
            g2.setColor(ACTIVE_COLOR);
            g2.fillRect(0, 0, width, height);
        }
        // 2. Dibujar el fondo de hover
        else if (hovering) {
            g2.setColor(HOVER_COLOR);
            g2.fillRect(0, 0, width, height);
        }

        // 3. Dibujar el fondo de presionado
        if (getModel().isPressed()) {
            g2.setColor(PRESSED_COLOR);
            g2.fillRect(0, 0, width, height);
        }

        g2.dispose();

        // Llama al super para dibujar el texto y el ícono
        super.paintComponent(g);
    }
}