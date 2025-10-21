package org.paq;

import org.paq.UI.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*; // Importa todos los eventos
import java.net.URL;
import com.google.gson.Gson;
import java.net.http.HttpClient;
import java.net.URI; // Import faltante
import java.net.http.HttpRequest; // Import faltante
import java.net.http.HttpResponse; // Import faltante
import com.google.gson.JsonObject; // Import faltante
import com.google.gson.JsonArray; // Import faltante
import com.google.gson.JsonElement; // Import faltante
import com.toedter.calendar.JCalendar; // Import JCalendar
import java.util.Date; // Import Date
import java.text.SimpleDateFormat; // Import SimpleDateFormat
import java.beans.PropertyChangeListener; // Import PropertyChangeListener
import java.beans.PropertyChangeEvent; // Import PropertyChangeEvent

public class AdminDashboard extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cards);
    private final String usuarioId;
    private final HttpClient httpClient;
    private final Gson gson;
    private final com.toedter.calendar.JCalendar scheduleCalendar; // Variable para el calendario

    // Constructor actualizado
    public AdminDashboard(String userId) {
        super("FoodFlow – Administrador");

        this.usuarioId = userId;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.scheduleCalendar = new com.toedter.calendar.JCalendar(); // Inicializa el calendario

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // ===== PANEL RAÍZ (ROOT) =====
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // ===== 1. BARRA LATERAL (SIDEBAR) =====
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // ===== 2. CONTENIDO PRINCIPAL =====
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Color.WHITE);
        contentWrapper.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel logo = loadLogo();
        logo.setHorizontalAlignment(SwingConstants.RIGHT);
        contentWrapper.add(logo, BorderLayout.NORTH);

        mainContentPanel.setBackground(Color.WHITE);
        contentWrapper.add(mainContentPanel, BorderLayout.CENTER);

        // --- Crear todas las vistas ---
        JPanel menuView = createMainMenuView();
        JPanel inventarioView = createInventarioView();
        JPanel nominasView = createNominasView();
        JPanel horariosView = createHorariosView();
        JPanel ajustesView = createAjustesView();
        JPanel rutasView = createRutasView();
        JPanel ayudaView = createAyudaView();
        JPanel ajustesUsuariosView = createAjustesUsuariosView();
        JPanel ajustesRutasView = createAjustesRutasView();

        // --- Añadir vistas al CardLayout ---
        mainContentPanel.add(menuView, "MENU");
        mainContentPanel.add(nominasView, "NOMINAS");
        mainContentPanel.add(horariosView, "HORARIOS");
        mainContentPanel.add(ajustesView, "AJUSTES");
        mainContentPanel.add(rutasView, "RUTAS");
        mainContentPanel.add(ayudaView, "AYUDA");
        mainContentPanel.add(inventarioView, "INVENTARIO");
        mainContentPanel.add(ajustesUsuariosView, "AJUSTES_USUARIOS");
        mainContentPanel.add(ajustesRutasView, "AJUSTES_RUTAS");

        root.add(contentWrapper, BorderLayout.CENTER);
        cards.show(mainContentPanel, "MENU"); // Empezar en el menú principal
    }

    // --- BARRA LATERAL ---
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIConstants.VERDE_800);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel adminName = new JLabel("Administrador");
        adminName.setForeground(Color.WHITE);
        adminName.setFont(UIConstants.TITLE.deriveFont(18f));
        adminName.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(adminName);
        sidebar.add(Box.createVerticalStrut(30));

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

        RoundedButton inventarioBtn = new RoundedButton("Inventario"); // Renombrado de usersBtn
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
                new FoodFlowLogin().setVisible(true);
            }
        });
        sidebar.add(logoutButton);

        return sidebar;
    }

    // --- MENÚ PRINCIPAL ---
    private JPanel createMainMenuView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel title = new JLabel("Menú Principal");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        panel.add(title, BorderLayout.NORTH);

        JPanel kpiCardsPanel = new JPanel(new GridLayout(1, 4, 30, 0));
        kpiCardsPanel.setOpaque(false);
        kpiCardsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/stats")).GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonObject stats = gson.fromJson(resp.body(), JsonObject.class);
                kpiCardsPanel.add(createKpiCard("Ingresos Totales", stats.get("ingresosTotales").getAsString(), "cash.png"));
                kpiCardsPanel.add(createKpiCard("Pedidos del Día", stats.get("pedidosDia").getAsString(), "clipboard.png"));
                kpiCardsPanel.add(createKpiCard("Nuevos Clientes", stats.get("nuevosClientes").getAsString(), "new-user.png"));
                kpiCardsPanel.add(createKpiCard("Empleados Activos", stats.get("empleadosActivos").getAsString(), "delivery-man.png"));
            } else { throw new Exception("Error al cargar stats: " + resp.statusCode()); }
        } catch (Exception e) {
            e.printStackTrace();
            // Añadir tarjetas de error
            kpiCardsPanel.add(createKpiCard("Ingresos Totales", "Error", "cash.png"));
            // ... (añadir las otras 3 tarjetas de error)
        }
        panel.add(kpiCardsPanel, BorderLayout.CENTER);
        return panel;
    }

    // --- INVENTARIO ---
    private JPanel createInventarioView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Inventario");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        headerPanel.add(title, BorderLayout.WEST);

        RoundedButton newProductButton = new RoundedButton("Nuevo Producto");
        headerPanel.add(newProductButton, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));

        String[] columnNames = {"ID Producto", "Nombre", "Cantidad", "Acciones"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable inventoryTable = new JTable(tableModel);

        // TODO: Conectar al backend /api/inventario
        tableModel.addRow(new Object[]{"PROD-001", "Producto A", 50, ""});
        tableModel.addRow(new Object[]{"PROD-002", "Producto B", 120, ""});

        card.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // --- NOMINAS ---
    private JPanel createNominasView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
        JLabel title = new JLabel("Pagos y Nóminas");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JLabel("Aquí irá el contenido para gestionar las nóminas."), BorderLayout.CENTER);
        return panel;
    }

    // --- HORARIOS ---
    private JPanel createHorariosView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel topPanel = new JPanel(new BorderLayout(20, 0));
        topPanel.setOpaque(false);

        // --- Tabla (Inferior) ---
        String[] columnNames = {"Empleado", "Hora de Entrada", "Hora de Salida"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable scheduleTable = new JTable(tableModel);
        scheduleTable.setFont(UIConstants.BASE.deriveFont(14f));
        scheduleTable.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // --- Formulario "Añadir Horario" (Izquierda-Superior) ---
        JPanel addHorarioPanel = createAddHorarioPanel(tableModel);
        topPanel.add(addHorarioPanel, BorderLayout.CENTER);

        // --- Calendario JCalendar (Derecha-Superior) ---
        this.scheduleCalendar.setBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE));
        JPanel calendarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        calendarPanel.setOpaque(false);
        calendarPanel.add(this.scheduleCalendar);
        topPanel.add(calendarPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- Conector (Listener) ---
        this.scheduleCalendar.addPropertyChangeListener("calendar", evt -> {
            actualizarTablaHorarios(tableModel);
        });

        // Carga inicial
        actualizarTablaHorarios(tableModel);

        return panel;
    }

    // --- AJUSTES (Menú de Tarjetas) ---
    private JPanel createAjustesView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel title = new JLabel("Ajustes");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        panel.add(title, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 30, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(40, 0, 0, 0));

        cardsPanel.add(createSettingsCard("Ajustes usuarios", "user.png", "AJUSTES_USUARIOS"));
        cardsPanel.add(createSettingsCard("Vista rutas", "delivery-man.png", "AJUSTES_RUTAS"));
        cardsPanel.add(createSettingsCard("Inventarios", "clipboard.png", "INVENTARIO"));
        cardsPanel.add(createSettingsCard("Pagos y Nóminas", "cash.png", "NOMINAS"));

        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        panel.add(cardsPanel, BorderLayout.CENTER);
        return panel;
    }

    // --- TABLERO RUTAS ---
    private JPanel createRutasView() {
        JPanel panel = new JPanel(new BorderLayout(30, 0)); // Con espacio horizontal
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/rutas/hoy")).GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonObject datos = gson.fromJson(resp.body(), JsonObject.class);
                JsonArray rutasActivas = datos.getAsJsonArray("rutas_activas");
                JsonArray rutasCompletadas = datos.getAsJsonArray("rutas_completadas");
                JsonObject progreso = datos.getAsJsonObject("progreso");

                JPanel rutasColumn = createRutasColumn(rutasActivas);
                panel.add(rutasColumn, BorderLayout.CENTER);
                JPanel statsColumn = createStatsColumn(progreso, rutasCompletadas);
                panel.add(statsColumn, BorderLayout.EAST);
            } else { panel.add(new JLabel("Error al cargar datos de rutas: " + resp.body())); }
        } catch (Exception e) { e.printStackTrace(); panel.add(new JLabel("Error de conexión con el servidor.")); }
        return panel;
    }

    // --- AYUDA ---
    private JPanel createAyudaView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
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
        // Quitamos el logo duplicado de aquí
        panel.add(headerPanel, BorderLayout.NORTH);

        JTextArea messageArea = new JTextArea();
        messageArea.setFont(UIConstants.BASE.deriveFont(16f));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        RoundedButton backButton = new RoundedButton("Regresar al Menú");
        backButton.addActionListener(e -> cards.show(mainContentPanel, "MENU"));
        RoundedButton sendButton = new RoundedButton("Enviar");
        sendButton.addActionListener(e -> {
            showThankYouDialog();
            messageArea.setText("");
            cards.show(mainContentPanel, "MENU");
        });
        buttonsPanel.add(backButton);
        buttonsPanel.add(sendButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        return panel;
    }

    // --- VISTA AJUSTES USUARIOS ---
    private JPanel createAjustesUsuariosView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel title = new JLabel("Ajustes usuarios");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        panel.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));

        String[] columnNames = {"IDUSER", "Nombre", "Accion"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable usersTable = new JTable(tableModel);

        // TODO: Conectar a /api/admin/usuarios y añadir botones de acción
        tableModel.addRow(new Object[]{"1", "Juan Perez", ""});
        tableModel.addRow(new Object[]{"2", "Pedro Sanchez", ""});

        card.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        RoundedButton addButton = new RoundedButton("+ Add");
        // addButton.addActionListener(e -> showCreateUserDialog()); // Pendiente
        footer.add(addButton);
        card.add(footer, BorderLayout.SOUTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // --- VISTA AJUSTES RUTAS ---
    private JPanel createAjustesRutasView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel title = new JLabel("Vista rutas");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        panel.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));

        String[] columnNames = {"IDUSER", "Nombre", "Accion"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable rutasTable = new JTable(tableModel);

        // TODO: Conectar a /api/admin/rutas (o similar) y añadir botones de acción
        tableModel.addRow(new Object[]{"1", "Juan Perez", ""});
        tableModel.addRow(new Object[]{"2", "Pedro Sanchez", ""});

        card.add(new JScrollPane(rutasTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        RoundedButton addButton = new RoundedButton("+ Add");
        // addButton.addActionListener(e -> showCreateRutaDialog()); // Ya implementado en Tablero Rutas
        footer.add(addButton);
        card.add(footer, BorderLayout.SOUTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }


    private JPanel createRutasColumn(JsonArray rutasActivas) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        JPanel header = createRutasHeader();
        panel.add(header, BorderLayout.NORTH);
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridLayout(0, 1, 0, 15)); // Layout corregido
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        for (JsonElement e : rutasActivas) { listPanel.add(createRutaCard(e.getAsJsonObject())); }
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null); scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    private JPanel createStatsColumn(JsonObject progreso, JsonArray rutasCompletadas) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(350, 10));
        panel.add(createProgresoCard(progreso));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createCompletadasCard(rutasCompletadas));
        panel.add(Box.createVerticalGlue());
        return panel;
    }
    private JPanel createRutasHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Rutas para hoy");
        title.setFont(UIConstants.TITLE.deriveFont(24f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Today");
        subtitle.setFont(UIConstants.BASE.deriveFont(16f));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);
        RoundedButton addButton = new RoundedButton("+ Añadir");
        addButton.addActionListener(e -> showCreateRutaDialog());
        header.add(addButton, BorderLayout.EAST);
        return header;
    }
    private JPanel createRutaCard(JsonObject ruta) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(15, 20, 15, 20)));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel(ruta.get("nombre").getAsString());
        title.setFont(UIConstants.TITLE.deriveFont(18f));
        header.add(title, BorderLayout.WEST);
        JLabel optionsLabel = new JLabel("...");
        optionsLabel.setFont(UIConstants.TITLE.deriveFont(Font.BOLD, 18f));
        optionsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.add(optionsLabel, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        JPanel details = new JPanel(new GridLayout(4, 2, 0, 5));
        details.setOpaque(false);
        details.add(new JLabel("ID empleado:"));
        details.add(new JLabel(ruta.get("empId").getAsString()));
        details.add(new JLabel("Prioridad:"));
        details.add(new JLabel(ruta.get("prioridad").getAsString()));
        details.add(new JLabel("Estatus:"));
        details.add(new JLabel(ruta.get("estatus").getAsString()));
        details.add(new JLabel("Creado el:"));
        details.add(new JLabel(ruta.get("creado").getAsString().substring(0, 10)));
        card.add(details, BorderLayout.CENTER);
        return card;
    }
    private JPanel createProgresoCard(JsonObject progreso) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel title = new JLabel("Progreso actual");
        title.setFont(UIConstants.BASE.deriveFont(16f));
        title.setForeground(Color.GRAY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        int completadas = progreso.get("completadas").getAsInt();
        int total = progreso.get("total").getAsInt();
        JLabel progressLabel = new JLabel(String.format("%d de %d para hoy", completadas, total));
        progressLabel.setFont(UIConstants.TITLE.deriveFont(32f));
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(progressLabel);
        return card;
    }
    private JPanel createCompletadasCard(JsonArray rutasCompletadas) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Rutas completadas");
        title.setFont(UIConstants.TITLE.deriveFont(18f));
        header.add(title, BorderLayout.WEST);
        card.add(header, BorderLayout.NORTH);
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        for (JsonElement e : rutasCompletadas) {
            listPanel.add(createRutaCompletadaItem(e.getAsJsonObject()));
            listPanel.add(Box.createVerticalStrut(15));
        }
        card.add(listPanel, BorderLayout.CENTER);
        return card;
    }
    private JPanel createRutaCompletadaItem(JsonObject ruta) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);
        JLabel title = new JLabel(ruta.get("nombre").getAsString());
        title.setFont(UIConstants.TITLE.deriveFont(16f));
        item.add(title);
        String detailsText = String.format("ID: %s | Estatus: %s | Creado: %s", ruta.get("empId").getAsString(), ruta.get("estatus").getAsString(), ruta.get("creado").getAsString().substring(0, 10));
        JLabel details = new JLabel(detailsText);
        details.setFont(UIConstants.BASE.deriveFont(12f));
        details.setForeground(Color.GRAY);
        item.add(details);
        return item;
    }
    // --- MÉTODOS DE AYUDA PARA HORARIOS ---
    private JPanel createAddHorarioPanel(DefaultTableModel tableModel) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("Añadir Horario");
        title.setFont(UIConstants.TITLE.deriveFont(24f));
        panel.add(title, gbc);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Empleado:"), gbc);
        gbc.gridx = 1;
        JComboBox<UserItem> fEmpleado = new JComboBox<>();
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/usuarios")).GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonArray usuarios = gson.fromJson(resp.body(), JsonArray.class);
                fEmpleado.addItem(new UserItem(0, "Seleccionar Empleado..."));
                for (JsonElement userElement : usuarios) {
                    JsonObject usuario = userElement.getAsJsonObject();
                    if (usuario.get("rol").getAsString().equals("empleado")) {
                        fEmpleado.addItem(new UserItem(usuario.get("id").getAsInt(), usuario.get("nombre").getAsString())); }
                }
            } else { fEmpleado.addItem(new UserItem(-1, "Error al cargar")); }
        } catch (Exception e) { fEmpleado.addItem(new UserItem(-1, "Error")); }
        panel.add(fEmpleado, gbc);
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Hora Entrada (HH:mm):"), gbc);
        gbc.gridx = 1;
        JTextField fEntrada = new JTextField("09:00");
        panel.add(fEntrada, gbc);
        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Hora Salida (HH:mm):"), gbc);
        gbc.gridx = 1;
        JTextField fSalida = new JTextField("17:00");
        panel.add(fSalida, gbc);
        gbc.gridy = 4;
        gbc.gridx = 1;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        RoundedButton btnGuardar = new RoundedButton("Guardar Horario");
        btnGuardar.addActionListener(e -> guardarHorario(fEmpleado, fEntrada.getText(), fSalida.getText(), "Laboral", tableModel));
        RoundedButton btnDescanso = new RoundedButton("Marcar Descanso");
        btnDescanso.addActionListener(e -> guardarHorario(fEmpleado, "00:00", "00:00", "Descanso", tableModel));
        buttonPanel.add(btnDescanso);
        buttonPanel.add(btnGuardar);
        panel.add(buttonPanel, gbc);
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        panel.add(new JLabel(""), gbc); // Espacio extra
        return panel;
    }
    private void guardarHorario(JComboBox<UserItem> fEmpleado, String entrada, String salida, String estatus, DefaultTableModel tableModel) {
        UserItem empleado = (UserItem) fEmpleado.getSelectedItem();
        Date fechaSeleccionada = this.scheduleCalendar.getDate();
        String fechaFormateada = new SimpleDateFormat("yyyy-MM-dd").format(fechaSeleccionada);
        if (empleado == null || empleado.getId() == 0) { JOptionPane.showMessageDialog(this, "Selecciona un empleado.", "Error", JOptionPane.WARNING_MESSAGE); return; }
        // TODO: Validar formato HH:mm
        JsonObject payload = new JsonObject();
        payload.addProperty("empleado_id", empleado.getId()); payload.addProperty("fecha", fechaFormateada);
        payload.addProperty("entrada", entrada); payload.addProperty("salida", salida); payload.addProperty("estatus", estatus);
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/horarios/crear")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload))).build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 201) {
                JOptionPane.showMessageDialog(this, "Horario guardado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                actualizarTablaHorarios(tableModel);
            } else { throw new Exception(resp.body()); }
        } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
    private void actualizarTablaHorarios(DefaultTableModel tableModel) {
        Date selectedDate = this.scheduleCalendar.getDate();
        String fechaFormateada = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
        tableModel.setRowCount(0);
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/horarios-dia/" + fechaFormateada)).GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonArray horarios = gson.fromJson(resp.body(), JsonArray.class);
                if (horarios.size() == 0) { tableModel.addRow(new Object[]{"Sin horarios asignados", "", ""}); }
                else {
                    for (JsonElement e : horarios) {
                        JsonObject horario = e.getAsJsonObject();
                        tableModel.addRow(new Object[]{ horario.get("empleado").getAsString(), horario.get("entrada").getAsString(), horario.get("salida").getAsString() });
                    }
                }
            } else { tableModel.addRow(new Object[]{"Error al cargar", "", ""}); }
        } catch (Exception ex) { ex.printStackTrace(); tableModel.addRow(new Object[]{"Error de conexión", "", ""}); }
    }
    // --- MÉTODOS DE AYUDA PARA AJUSTES ---
    private JPanel createSettingsCard(String text, String iconName, String cardKey) {
        JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBackground(UIConstants.VERDE_700);
        card.setBorder(new EmptyBorder(30, 20, 30, 20)); card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(UIConstants.VERDE_800); }
            @Override public void mouseExited(MouseEvent e) { card.setBackground(UIConstants.VERDE_700); }
            @Override public void mouseClicked(MouseEvent e) { cards.show(mainContentPanel, cardKey); }
        });
        URL res = getClass().getResource("/icons/" + iconName);
        if (res != null) {
            JLabel iconLabel = new JLabel(new ImageIcon(new ImageIcon(res).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(iconLabel);
        }
        JLabel textLabel = new JLabel(text); textLabel.setForeground(Color.WHITE); textLabel.setFont(UIConstants.BASE.deriveFont(Font.BOLD, 16f)); textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(20)); card.add(textLabel);
        return card;
    }
    // --- MÉTODOS DE AYUDA GENERALES (KPI, Diálogos, Logo, UserItem) ---
    private JPanel createKpiCard(String title, String value, String iconName) {
        JPanel card = new JPanel(new BorderLayout(15, 0)); card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));
        JLabel iconLabel = new JLabel(); URL res = getClass().getResource("/icons/" + iconName);
        if (res != null) { iconLabel.setIcon(new ImageIcon(new ImageIcon(res).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH))); }
        card.add(iconLabel, BorderLayout.WEST);
        JPanel textPanel = new JPanel(); textPanel.setOpaque(false); textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JLabel valueLabel = new JLabel(value); valueLabel.setFont(UIConstants.TITLE.deriveFont(24f));
        JLabel titleLabel = new JLabel(title); titleLabel.setFont(UIConstants.BASE.deriveFont(14f)); titleLabel.setForeground(Color.GRAY);
        textPanel.add(valueLabel); textPanel.add(titleLabel); card.add(textPanel, BorderLayout.CENTER);
        return card;
    }
    private void showCreateRutaDialog() {
        JDialog dialog = new JDialog(this, "Crear Nueva Ruta", true);
        try {
            JPanel formPanel = createRutaFormPanel(dialog);
            dialog.setContentPane(formPanel); dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
        } catch (Exception e) { e.printStackTrace(); JOptionPane.showMessageDialog(this, "Error al cargar.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
    private JPanel createRutaFormPanel(JDialog dialog) throws Exception {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nombre Ruta:"), gbc); gbc.gridx = 1; JTextField fNombre = new JTextField(20); panel.add(fNombre, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Prioridad:"), gbc); gbc.gridx = 1; String[] prioridades = {"Baja", "Media", "Alta"}; JComboBox<String> fPrioridad = new JComboBox<>(prioridades); fPrioridad.setSelectedItem("Media"); panel.add(fPrioridad, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Asignar a:"), gbc); gbc.gridx = 1; JComboBox<UserItem> fEmpleado = new JComboBox<>();
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/usuarios")).GET().build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            JsonArray usuarios = gson.fromJson(resp.body(), JsonArray.class); fEmpleado.addItem(new UserItem(0, "Sin Asignar"));
            for (JsonElement userElement : usuarios) {
                JsonObject usuario = userElement.getAsJsonObject();
                if (usuario.get("rol").getAsString().equals("empleado")) { fEmpleado.addItem(new UserItem(usuario.get("id").getAsInt(), usuario.get("nombre").getAsString())); }
            }
        } else { throw new Exception("Error al cargar empleados."); }
        panel.add(fEmpleado, gbc);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); RoundedButton cancelButton = new RoundedButton("Cancelar"); cancelButton.addActionListener(e -> dialog.dispose());
        RoundedButton createButton = new RoundedButton("Crear Ruta"); createButton.addActionListener(e -> {
            String nombre = fNombre.getText(); String prioridad = (String) fPrioridad.getSelectedItem(); UserItem empleado = (UserItem) fEmpleado.getSelectedItem(); int empleadoId = empleado.getId();
            if (nombre.isBlank()) { JOptionPane.showMessageDialog(dialog, "Nombre vacío.", "Error", JOptionPane.WARNING_MESSAGE); return; }
            JsonObject nuevaRuta = new JsonObject(); nuevaRuta.addProperty("nombre", nombre); nuevaRuta.addProperty("prioridad", prioridad); nuevaRuta.addProperty("empleado_id", empleadoId == 0 ? null : empleadoId);
            try {
                HttpRequest postReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/rutas/crear")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gson.toJson(nuevaRuta))).build();
                HttpResponse<String> postResp = httpClient.send(postReq, HttpResponse.BodyHandlers.ofString());
                if (postResp.statusCode() == 201) {
                    JOptionPane.showMessageDialog(dialog, "Ruta creada.", "Éxito", JOptionPane.INFORMATION_MESSAGE); dialog.dispose();
                    JPanel newRutasView = createRutasView(); mainContentPanel.add(newRutasView, "RUTAS"); cards.show(mainContentPanel, "RUTAS");
                } else { throw new Exception(postResp.body()); }
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(dialog, "Error al crear:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });
        buttonPanel.add(cancelButton); buttonPanel.add(createButton); gbc.gridx = 1; gbc.gridy = 3; panel.add(buttonPanel, gbc);
        return panel;
    }
    private void showThankYouDialog() {
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
    private JLabel loadLogo() {
        JLabel logoLabel = new JLabel(); URL res = getClass().getResource("/ima/Food1.png");
        if (res != null) {
            ImageIcon icon = new ImageIcon(res); Image scaled = icon.getImage().getScaledInstance(160, -1, Image.SCALE_SMOOTH); logoLabel.setIcon(new ImageIcon(scaled));
        } else { logoLabel.setText("FoodFlow"); logoLabel.setFont(UIConstants.LOGO); }
        return logoLabel;
    }
    private static class UserItem {
        private final int id; private final String nombre;
        public UserItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        public int getId() { return id; }
        @Override public String toString() { return nombre; }
    }

} // Fin de la clase AdminDashboard