package org.paq;

import org.paq.UI.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class EmpleadoDashboard extends JFrame {

    // El "mazo de cartas" para cambiar entre vistas
    private final CardLayout cards = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cards);

    public EmpleadoDashboard(String userId) {
        super("FoodFlow – Empleado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // ===== PANEL RAÍZ (ROOT) =====
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // ===== 1. BARRA LATERAL (SIDEBAR) =====
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // ===== 2. CONTENIDO PRINCIPAL (con CardLayout) =====
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Color.WHITE); // Fondo blanco general
        contentWrapper.setBorder(new EmptyBorder(40, 50, 40, 50)); // Margen exterior
        JLabel logo = loadLogo();
        logo.setHorizontalAlignment(SwingConstants.RIGHT);
        contentWrapper.add(logo, BorderLayout.NORTH);

        mainContentPanel.setBackground(Color.WHITE);
        contentWrapper.add(mainContentPanel, BorderLayout.CENTER);

        // --- Crear y añadir todas las vistas al mazo de cartas ---
        JPanel menuView = createMainMenuView();
        JPanel profileView = createProfileView();
        JPanel receiptsView = createReceiptsView();
        JPanel schedulesView = createSchedulesView();
        JPanel deliveriesView = createDeliveriesView();

        mainContentPanel.add(menuView, "MENU");
        mainContentPanel.add(profileView, "PERFIL");
        mainContentPanel.add(receiptsView, "RECIBOS");
        mainContentPanel.add(schedulesView, "HORARIOS");
        mainContentPanel.add(deliveriesView, "REPARTOS");

        root.add(contentWrapper, BorderLayout.CENTER);

        // Mostrar el menú principal por defecto
        cards.show(mainContentPanel, "MENU");
    }


    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIConstants.VERDE_800);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel employeeName = new JLabel("Empleado 1");
        employeeName.setForeground(Color.WHITE);
        employeeName.setFont(UIConstants.TITLE.deriveFont(18f));
        employeeName.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(employeeName);
        sidebar.add(Box.createVerticalStrut(30));

        // --- Botones del Menú ---
        RoundedButton menuBtn = new RoundedButton("Menú Principal");
        menuBtn.addActionListener(e -> cards.show(mainContentPanel, "MENU"));
        sidebar.add(menuBtn);
        sidebar.add(Box.createVerticalStrut(10));

        RoundedButton recibosBtn = new RoundedButton("Nominas");
        recibosBtn.addActionListener(e -> cards.show(mainContentPanel, "RECIBOS"));
        sidebar.add(recibosBtn);
        sidebar.add(Box.createVerticalStrut(10));

        RoundedButton horariosBtn = new RoundedButton("Horarios");
        horariosBtn.addActionListener(e -> cards.show(mainContentPanel, "HORARIOS"));
        sidebar.add(horariosBtn);
        sidebar.add(Box.createVerticalStrut(10));

        RoundedButton deliveriesBtn = new RoundedButton("Recibos");
        deliveriesBtn.addActionListener(e -> cards.show(mainContentPanel, "REPARTOS"));
        sidebar.add(deliveriesBtn);
        sidebar.add(Box.createVerticalStrut(10));

        RoundedButton ayudaBtn = new RoundedButton("Ayuda");
        ayudaBtn.addActionListener(e -> showHelpDialog());
        sidebar.add(ayudaBtn);
        sidebar.add(Box.createVerticalStrut(10));

        // --- Botón de Cerrar Sesión ---
        sidebar.add(Box.createVerticalGlue());
        RoundedButton logoutButton = new RoundedButton("Logout");
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que quieres cerrar la sesión?",
                    "Confirmar Cierre de Sesión", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new FoodFlowLogin().setVisible(true);
            }
        });
        sidebar.add(logoutButton);

        return sidebar;
    }

    /**
     * Crea la vista del menú principal con las 4 tarjetas grandes interactivas.
     */
    private JPanel createMainMenuView() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        JPanel contentHolder = new JPanel();
        contentHolder.setOpaque(false);
        contentHolder.setLayout(new BoxLayout(contentHolder, BoxLayout.Y_AXIS));

        JLabel welcomeTitle = new JLabel("Hola empleado 1");
        welcomeTitle.setFont(UIConstants.TITLE.deriveFont(32f));
        welcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Que buscas hoy");
        subtitle.setFont(UIConstants.BASE.deriveFont(18f));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentHolder.add(welcomeTitle);
        contentHolder.add(Box.createVerticalStrut(10));
        contentHolder.add(subtitle);
        contentHolder.add(Box.createVerticalStrut(40));

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 30, 0));
        cardsPanel.setOpaque(false);

        // --- Hacemos las tarjetas clickeables ---
        JPanel recibosCard = createMenuCard("TUS RECIBOS", "exclamation.png");
        recibosCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cards.show(mainContentPanel, "RECIBOS");
            }
        });

        JPanel horariosCard = createMenuCard("TUS HORARIOS", "audit.png");
        horariosCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cards.show(mainContentPanel, "HORARIOS");
            }
        });

        JPanel repartosCard = createMenuCard("REPARTOS", "to-do-list.png");
        repartosCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cards.show(mainContentPanel, "REPARTOS");
            }
        });

        JPanel perfilCard = createMenuCard("PERFIL", "user.png");
        perfilCard.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cards.show(mainContentPanel, "PERFIL");
            }
        });

        cardsPanel.add(recibosCard);
        cardsPanel.add(horariosCard);
        cardsPanel.add(repartosCard);
        cardsPanel.add(perfilCard);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        contentHolder.add(cardsPanel);
        mainPanel.add(contentHolder, BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * Crea la vista de "Perfil" tal como se ve en la imagen.
     */
    private JPanel createProfileView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Perfil de Usuario");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        panel.add(title, BorderLayout.NORTH);

        // Panel de tarjeta para el contenido
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1),
                new EmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Foto de perfil
        JLabel profilePic = new JLabel();
        profilePic.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/icons/user.png")).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3; // Ocupa 3 filas de alto
        card.add(profilePic, gbc);

        // Nombre y Rol
        gbc.gridheight = 1; // Restablecer
        gbc.gridx = 1;
        JLabel nameLabel = new JLabel("Juan Pérez");
        nameLabel.setFont(UIConstants.TITLE.deriveFont(24f));
        card.add(nameLabel, gbc);

        gbc.gridy = 1;
        JLabel roleLabel = new JLabel("Repartidor");
        roleLabel.setFont(UIConstants.BASE.deriveFont(16f));
        roleLabel.setForeground(Color.GRAY);
        card.add(roleLabel, gbc);

        // Separador
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2; // Ocupa 2 columnas de ancho
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(new JSeparator(), gbc);

        // Información de contacto
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 4;
        card.add(new JLabel("ID Empleado:"), gbc);
        gbc.gridx = 1;
        card.add(new JLabel("EMP-001"), gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        card.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        card.add(new JLabel("33-1234-5678"), gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        card.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        card.add(new JLabel("juan.perez@foodflow.com"), gbc);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }


    private JPanel createReceiptsView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Nómina de Pedidos");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        panel.add(title, BorderLayout.NORTH);

        String[] columnNames = {"ID Pedido", "Fecha", "Monto", "Estado"};
        Object[][] data = {
                {"#1234", "15/10/2025", "$450.00", "Pagado"},
                {"#1235", "15/10/2025", "$320.50", "Pagado"},
                {"#1236", "16/10/2025", "$510.00", "Pendiente"},
        };
        JTable table = new JTable(new DefaultTableModel(data, columnNames));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }


    private JPanel createSchedulesView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Horarios de Trabajo");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        panel.add(title, BorderLayout.NORTH);

        String[] columnNames = {"Día", "Entrada", "Salida", "Notas"};
        Object[][] data = {
                {"Lunes", "09:00 AM", "05:00 PM", "Ruta Norte"},
                {"Martes", "09:00 AM", "05:00 PM", "Ruta Sur"},
                {"Miércoles", "Descanso", "Descanso", ""},
                {"Jueves", "10:00 AM", "06:00 PM", "Ruta Centro"},
        };
        JTable table = new JTable(new DefaultTableModel(data, columnNames));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }


    private JPanel createDeliveriesView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Repartos del Día");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        panel.add(title, BorderLayout.NORTH);

        String[] columnNames = {"ID Pedido", "Cliente", "Dirección", "Estado"};
        Object[][] data = {
                {"#2512", "Ana García", "Av. Siempre Viva 123", "En camino"},
                {"#2513", "Luis Torres", "Calle Falsa 456", "Pendiente"},
                {"#2514", "Sofía Ramirez", "Blvd. de los Sueños 789", "Pendiente"},
        };
        JTable table = new JTable(new DefaultTableModel(data, columnNames));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }


    // --- Métodos de ayuda que ya tenías ---
    private JLabel loadLogo() {
        JLabel logoLabel = new JLabel();
        URL res = getClass().getResource("/ima/Food1.png");
        if (res != null) {
            ImageIcon icon = new ImageIcon(res);
            Image scaled = icon.getImage().getScaledInstance(160, -1, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaled));
        } else {
            logoLabel.setText("FoodFlow");
            logoLabel.setFont(UIConstants.LOGO);
        }
        return logoLabel;
    }

    private JPanel createMenuCard(String text, String iconName) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.VERDE_700);
        card.setBorder(new EmptyBorder(30, 20, 30, 20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(UIConstants.VERDE_800);
            }

            public void mouseExited(MouseEvent e) {
                card.setBackground(UIConstants.VERDE_700);
            }
        });
        URL res = getClass().getResource("/icons/" + iconName);
        if (res != null) {
            JLabel iconLabel = new JLabel(new ImageIcon(new ImageIcon(res).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(iconLabel);
        }
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(UIConstants.BASE.deriveFont(Font.BOLD, 16f));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(20));
        card.add(textLabel);
        return card;
    }

    private void showHelpDialog() {
        JDialog helpDialog = new JDialog(this, "Centro de Ayuda", true);
        helpDialog.setSize(800, 600);
        helpDialog.setLocationRelativeTo(this);

        // CAMBIO: Le pasamos el propio diálogo al método que crea el contenido.
        JPanel helpContent = createHelpFormView(helpDialog);
        helpDialog.add(helpContent);

        helpDialog.setVisible(true);
    }

    private JPanel createHelpFormView(JDialog dialog) {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        // --- Encabezado con Título a la Izquierda y Logo a la Derecha ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Sub-panel para los títulos
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Ayuda");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Describe tu problema o duda a continuación:");
        subtitle.setFont(UIConstants.BASE.deriveFont(18f));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(10));
        titlePanel.add(subtitle);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(loadLogo(), BorderLayout.EAST); // <-- AÑADIMOS EL LOGO AQUÍ

        panel.add(headerPanel, BorderLayout.NORTH);

        // --- Área de Texto para el Mensaje ---
        JTextArea messageArea = new JTextArea();
        messageArea.setFont(UIConstants.BASE.deriveFont(16f));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        // --- Botones en la parte inferior ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        RoundedButton backButton = new RoundedButton("Regresar al Menú");
        backButton.addActionListener(e -> dialog.dispose());

        RoundedButton sendButton = new RoundedButton("Enviar");
        sendButton.addActionListener(e -> {
            dialog.dispose();
            showThankYouDialog();
        });

        buttonsPanel.add(backButton);
        buttonsPanel.add(sendButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showThankYouDialog() {
        JDialog thankYouDialog = new JDialog(this, "Confirmación", true);
        thankYouDialog.setSize(670, 320); // Un poco más alto para el logo
        thankYouDialog.setLocationRelativeTo(this);
        thankYouDialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.VERDE_700, 2),
                new EmptyBorder(15, 25, 25, 25) // Ajustamos el padding
        ));

        // --- 1. Cabecera con el Logo ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(loadLogo(), BorderLayout.EAST); // <-- AÑADIMOS EL LOGO AQUÍ
        panel.add(header, BorderLayout.NORTH);

        // --- 2. Contenido Central (Icono y Texto) ---
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));



        // Textos
        JLabel title = new JLabel("¡GRACIAS!");
        title.setFont(UIConstants.TITLE.deriveFont(28f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel("Tu mensaje se ha enviado con éxito.");
        message.setFont(UIConstants.BASE.deriveFont(16f));
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(title);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(message);
        panel.add(contentPanel, BorderLayout.CENTER);

        // --- 3. Botón "Aceptar" ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        RoundedButton okButton = new RoundedButton("Aceptar");
        okButton.setPreferredSize(new Dimension(120, 40));
        okButton.addActionListener(e -> thankYouDialog.dispose());
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        thankYouDialog.add(panel);
        thankYouDialog.setVisible(true);
    }
}