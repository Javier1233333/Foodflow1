package org.paq.UI;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LinkLabel extends JLabel {

    public LinkLabel(String text) {
        super(text);
        setFont(UIConstants.BASE.deriveFont(Font.BOLD));
        setForeground(UIConstants.VERDE_700);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                setText("<html><u>" + text + "</u></html>");
            }
            @Override public void mouseExited(MouseEvent e) {
                setText(text);
            }
        });
    }
}
