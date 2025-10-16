package org.paq;

import javax.swing.*;
import java.awt.*;

public class SeleccionModo extends JDialog {
    public interface ModoListener { void onAdmin(); void onEmpleado(); }
    public SeleccionModo(JFrame owner, ModoListener listener) {
        super(owner, "Selecciona el modo", true);
        var admin = new JButton("Administrador");
        var emp   = new JButton("Empleado");

        admin.addActionListener(e -> { listener.onAdmin(); dispose(); });
        emp.addActionListener(e -> { listener.onEmpleado(); dispose(); });

        var p = new JPanel(new GridLayout(1,2,16,16));
        p.add(emp); p.add(admin);
        var wrap = new JPanel(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        wrap.add(new JLabel("Selecciona el modo"), BorderLayout.NORTH);
        wrap.add(p, BorderLayout.CENTER);
        setContentPane(wrap);
        pack();
        setLocationRelativeTo(owner);
    }
}