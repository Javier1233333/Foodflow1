package org.paq;

import com.google.gson.Gson;
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;
import org.paq.admin.*;
import org.paq.admin.AdminViewContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.net.http.HttpClient;

// NOTA: Esta clase asume que AdminViewContext fue modificado para aceptar 'Long idUsuarioLong'
//       como su último argumento, y que las vistas lo usan.

public class AdminDashboard extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cards);

    // CAMBIO: Usaremos el ID numérico (Long) como identificador principal
    private final Long idUsuarioLong;

    // Objetos de dependencia
    private final HttpClient httpClient;
    private final Gson gson;
    private final com.toedter.calendar.JCalendar scheduleCalendar;

    /**
     * Clase de ayuda PÚBLICA para que todos los paneles la puedan usar.
     */
    public static class UserItem {
        private final int id;
        private final String nombre;
        private final double tarifaHora;
        public UserItem(int id, String nombre, double tarifaHora) {
            this.id = id;
            this.nombre = nombre;
            this.tarifaHora = tarifaHora;
        }
        public int getId() { return id; }
        public double getTarifaHora(){return tarifaHora;}
        @Override public String toString() { return nombre; }
    }

    // Constructor que recibe el ID numérico
    public AdminDashboard(Long idUsuarioLong) { // <-- AHORA ESPERA UN LONG
        super("FoodFlow – Administrador");

        this.idUsuarioLong = idUsuarioLong; // Asignación del ID numérico
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.scheduleCalendar = new com.toedter.calendar.JCalendar();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Color.WHITE);
        contentWrapper.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel logo = loadLogo();
        logo.setHorizontalAlignment(SwingConstants.RIGHT);
        contentWrapper.add(logo, BorderLayout.NORTH);

        mainContentPanel.setBackground(Color.WHITE);
        contentWrapper.add(mainContentPanel, BorderLayout.CENTER);

        // --- Creación de las Vistas ---

        // Creamos el "contexto" pasando el ID numérico como último argumento
        AdminViewContext context = new AdminViewContext(
                httpClient,
                gson,
                cards,
                mainContentPanel,
                scheduleCalendar,
                String.valueOf(idUsuarioLong) // Se convierte el Long a String si el Contexto aún espera String
        );

        // Inicialización de las vistas
        JPanel menuView = new MainMenuView(context);
        JPanel rutasView = new RutasViewPanel(context);
        JPanel horariosView = new HorariosViewPanel(context);
        JPanel inventarioView = new InventarioViewPanel(context);
        JPanel ajustesView = new AjustesViewPanel(context);
        JPanel nominasView = new NominasViewPanel(context);
        JPanel ayudaView = new AyudaViewPanel(context, this);
        JPanel ajustesUsuariosView = new AjustesUsuariosViewPanel(context);
        JPanel ajustesRutasView = new AjustesRutasViewPanel(context);

        // --- Añadimos las vistas al CardLayout ---
        mainContentPanel.add(menuView, "MENU");
        mainContentPanel.add(rutasView, "RUTAS");
        mainContentPanel.add(horariosView, "HORARIOS");
        mainContentPanel.add(inventarioView, "INVENTARIO");
        mainContentPanel.add(ajustesView, "AJUSTES");
        mainContentPanel.add(nominasView, "NOMINAS");
        mainContentPanel.add(ayudaView, "AYUDA");
        mainContentPanel.add(ajustesUsuariosView, "AJUSTES_USUARIOS");
        mainContentPanel.add(ajustesRutasView, "AJUSTES_RUTAS");

        root.add(contentWrapper, BorderLayout.CENTER);
        cards.show(mainContentPanel, "MENU");
    }

    // El Sidebar se queda aquí porque controla el CardLayout principal
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIConstants.VERDE_800);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBorder(new EmptyBorder(25, 20, 25, 20));

        // CAMBIO: Muestra el ID numérico
        JLabel adminName = new JLabel("ID: " + (idUsuarioLong != null ? idUsuarioLong.toString() : "N/A"));
        adminName.setForeground(Color.WHITE);
        adminName.setFont(UIConstants.TITLE.deriveFont(18f));
        adminName.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(adminName);
        sidebar.add(Box.createVerticalStrut(30));

        // --- Botones del Menú ---
        RoundedButton RutasBtn = new RoundedButton("Tablero Rutas");
        RutasBtn.addActionListener(e -> cards.show(mainContentPanel, "RUTAS"));
        sidebar.add(RutasBtn);
        sidebar.add(Box.createVerticalStrut(10));

        RoundedButton HorarioBtn = new RoundedButton("Horarios de empleados");
        HorarioBtn.addActionListener(e -> cards.show(mainContentPanel, "HORARIOS"));
        sidebar.add(HorarioBtn);
        sidebar.add(Box.createVerticalStrut(10));

        RoundedButton PagosBtn = new RoundedButton("Pagos y Nominas");
        PagosBtn.addActionListener(e -> cards.show(mainContentPanel, "NOMINAS"));
        sidebar.add(PagosBtn);
        sidebar.add(Box.createVerticalStrut(10));

        RoundedButton inventarioBtn = new RoundedButton("Inventario");
        inventarioBtn.addActionListener(e -> cards.show(mainContentPanel, "INVENTARIO"));
        sidebar.add(inventarioBtn);
        sidebar.add(Box.createVerticalStrut(10));

        RoundedButton AjustesBtn = new RoundedButton("Ajustes");
        AjustesBtn.addActionListener(e -> cards.show(mainContentPanel, "AJUSTES"));
        sidebar.add(AjustesBtn);
        sidebar.add(Box.createVerticalStrut(10));

        RoundedButton AyudaBtn = new RoundedButton("Ayuda");
        AyudaBtn.addActionListener(e -> cards.show(mainContentPanel, "AYUDA"));
        sidebar.add(AyudaBtn);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(Box.createVerticalGlue());
        RoundedButton logoutButton = new RoundedButton("Logout");
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro?", "Confirmar Cierre", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                // Redirige al login de usuario
                // new FoodFlowLogin().setVisible(true);
            }
        });
        sidebar.add(logoutButton);
        return sidebar;
    }

    public void showThankYouDialog() {
        JDialog thankYouDialog = new JDialog(this, "Confirmación", true);
        thankYouDialog.setSize(670, 320);
        thankYouDialog.setLocationRelativeTo(this);
        thankYouDialog.setUndecorated(true);
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.VERDE_700, 2),
                new EmptyBorder(15, 25, 25, 25)));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        header.add(loadLogo(), BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("¡GRACIAS!");
        title.setFont(UIConstants.TITLE.deriveFont(28f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel message = new JLabel("Tu mensaje se ha enviado.");
        message.setFont(UIConstants.BASE.deriveFont(16f));
        message.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(Box.createVerticalStrut(10)); contentPanel.add(title);
        contentPanel.add(Box.createVerticalStrut(10)); contentPanel.add(message);
        panel.add(contentPanel, BorderLayout.CENTER);
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

    public JLabel loadLogo() {
        JLabel logoLabel = new JLabel();
        URL res = getClass().getResource("/ima/ggg.png");
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
}