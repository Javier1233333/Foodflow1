package org.paq;


import javax.swing.*;
import java.awt.*;

public class VentanaPrototipo extends JFrame {
    public VentanaPrototipo(String titulo) {
        setTitle(titulo);
        setSize(640, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel lbl = new JLabel(titulo, SwingConstants.CENTER);
        lbl.setFont(new Font("Inter", Font.BOLD, 24));
        add(lbl, BorderLayout.CENTER);
    }
}