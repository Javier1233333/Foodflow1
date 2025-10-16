package org.paq;

import org.paq.UI.*;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminDashboard extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards);

    public AdminDashboard() {
        super("FoodFlow – Administrador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);

        // ====== LEFT SIDEBAR ======
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UIConstants.VERDE_700);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20,16,20,16));

        JLabel brand = new JLabel("FoodFlow");
        brand.setFont(UIConstants.LOGO);

        JLabel title = new JLabel("titulo");
        title.setFont(UIConstants.TITLE.deriveFont(22f));

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(10));

        Map<String,JPanel> vistas = new LinkedHashMap<>();
        vistas.put("Reparto",    new RepartoPanel());
        vistas.put("Nómina",     new NominaPanel());
        vistas.put("Horarios",   new HorariosPanel());
        vistas.put("Inventario", new InventarioPanel());
        vistas.put("Ajustes",    new AjustesPanel());
        vistas.put("Ayuda",      new AyudaPanelAdmin());

        vistas.forEach((k,v) -> center.add(wrapContent(k, v), k));

        Map<String, SideBarButton> botones = new LinkedHashMap<>();
        for (String nombre : vistas.keySet()) {
            SideBarButton b = new SideBarButton(nombre);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.addActionListener(e -> {
                cards.show(center, nombre);
                botones.forEach((k,btn) -> btn.setSelectedItem(k.equals(nombre)));
            });
            sidebar.add(b);
            sidebar.add(Box.createVerticalStrut(6));
            botones.put(nombre, b);
        }
        // Selecciona primero
        botones.values().iterator().next().setSelectedItem(true);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(10));

// Creamos el botón de logout usando nuestra clase personalizada
        RoundedButton logoutButton = new RoundedButton("Cerrar Sesión");
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, logoutButton.getPreferredSize().height));

        logoutButton.addActionListener(e -> {
            // 1. Muestra un diálogo de confirmación (opcional pero recomendado)
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que quieres cerrar la sesión?",
                    "Confirmar Cierre de Sesión",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // 2. Cierra la ventana actual del dashboard
                dispose();
                // 3. Abre una nueva ventana de login
                new FoodFlowLogin().setVisible(true);
            }
        });

        sidebar.add(logoutButton);


        // ====== ROOT ======
        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar, BorderLayout.WEST);

        JPanel bg = new JPanel(new BorderLayout());
        bg.setBackground(new Color(0xF3F4F6));    // fondo suave como la maqueta
        bg.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        bg.add(center, BorderLayout.CENTER);
        root.add(bg, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel wrapContent(String titulo, JPanel content) {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        // Encabezado
        JLabel title = new JLabel(titulo);
        title.setFont(new Font("Inter", Font.BOLD, 22));
        title.setForeground(UIConstants.TEXTO_SEC);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);

        CardPanel card = new CardPanel();
        card.add(content, BorderLayout.CENTER);

        page.add(header, BorderLayout.NORTH);
        page.add(Box.createVerticalStrut(12), BorderLayout.CENTER); // espaciador
        page.add(card, BorderLayout.SOUTH);

        // truco: usar otro contenedor para respetar márgenes
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.add(header);
        wrap.add(Box.createVerticalStrut(12));
        wrap.add(card);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.add(wrap, BorderLayout.NORTH);
        return outer;
    }
}
