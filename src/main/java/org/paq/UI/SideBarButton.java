package org.paq.UI;


import javax.swing.*;
import java.awt.*;

public class SideBarButton extends JButton {
    private boolean selected = false;

    public SideBarButton(String text) {
        super(text);
        setHorizontalAlignment(LEFT);
        setForeground(Color.WHITE);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setSelectedItem(boolean sel) { selected = sel; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // “pill” claro cuando está seleccionado
        if (selected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,32));
            g2.fillRoundRect(8, 4, getWidth()-16, getHeight()-8, 12, 12);
            g2.dispose();
        }
    }
}
