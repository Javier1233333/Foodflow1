package org.paq.UI;

import javax.swing.*;
import java.awt.*;

public class PrimaryButton extends JButton {
    public PrimaryButton(String text) {
        super(text);
        setForeground(Color.WHITE);
        setBackground(UIConstants.VERDE_700);
        setFont(UIConstants.BASE.deriveFont(Font.BOLD));
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e){ setBackground(UIConstants.VERDE_800); }
            @Override public void mouseExited (java.awt.event.MouseEvent e){ setBackground(UIConstants.VERDE_700); }
        });
    }
}
