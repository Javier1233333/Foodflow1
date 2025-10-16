package org.paq.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class RoundedInput extends JPanel {
    private boolean focused = false;

    public RoundedInput(JComponent inner, JLabel icon) {
        super(new BorderLayout(12, 0));
        setOpaque(false);

        // Tamaño FIJO tipo Figma (ajusta si quieres)
        int W = 520, H = 48; // ancho/alto Figma
        setPreferredSize(new Dimension(W, H));
        setMaximumSize(new Dimension(W, H));
        setMinimumSize(new Dimension(W, H));

        JPanel box = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // fondo
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));

                // borde 1.5 px (más “presente”)
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(focused ? UIConstants.VERDE_700 : UIConstants.BORDE_SUAVE);
                g2.draw(new RoundRectangle2D.Double(0.75, 0.75, getWidth()-1.5, getHeight()-1.5, 12, 12));
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(10, 14, 10, 14)); // padding interior Figma


        if (icon != null) {
            icon.setForeground(UIConstants.TEXTO_SEC);
            box.add(icon, BorderLayout.WEST);
        }

        // el campo real
        inner.setBorder(null);
        inner.setOpaque(false);
        inner.setFont(UIConstants.BASE);
        box.add(inner, BorderLayout.CENTER);

        // escuchar foco para pintar borde
        inner.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
            @Override public void focusLost  (FocusEvent e) { focused = false; repaint(); }
        });

        add(box, BorderLayout.CENTER);
    }
}
