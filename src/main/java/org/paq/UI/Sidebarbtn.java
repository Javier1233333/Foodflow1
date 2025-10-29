package org.paq.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Sidebarbtn extends JButton {

    public Sidebarbtn(String text) {
        super(text);
        setFont(UIConstants.BASE.deriveFont(Font.BOLD));
        setForeground(Color.WHITE);
        setBackground(UIConstants.VERDE_700);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                setBackground(UIConstants.VERDE_800);
            }
            @Override public void mouseExited(MouseEvent e) {
                setBackground(UIConstants.VERDE_700);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());

        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics(getFont());
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    @Override public void setContentAreaFilled(boolean b) {}
    @Override public void setBorderPainted(boolean b) {}
}
