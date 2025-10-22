package org.paq.views.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.paq.AdminDashboard; // Para UserItem
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RutasViewPanel extends JPanel {

    private final AdminViewContext context;

    public RutasViewPanel(AdminViewContext context) {
        super(new BorderLayout(30, 0));
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));
        initComponents();
    }

    private void initComponents() {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/rutas/hoy")).GET().build();
            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                JsonObject datos = context.gson.fromJson(resp.body(), JsonObject.class);
                JsonArray rutasActivas = datos.getAsJsonArray("rutas_activas");
                JsonArray rutasCompletadas = datos.getAsJsonArray("rutas_completadas");
                JsonObject progreso = datos.getAsJsonObject("progreso");

                JPanel rutasColumn = createRutasColumn(rutasActivas);
                this.add(rutasColumn, BorderLayout.CENTER);
                JPanel statsColumn = createStatsColumn(progreso, rutasCompletadas);
                this.add(statsColumn, BorderLayout.EAST);
            } else {
                this.add(new JLabel("Error al cargar datos de rutas: " + resp.body()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.add(new JLabel("Error de conexión con el servidor."));
        }
    }

    private JPanel createRutasColumn(JsonArray rutasActivas) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        JPanel header = createRutasHeader();
        panel.add(header, BorderLayout.NORTH);
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridLayout(0, 1, 0, 15));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        if (rutasActivas.size() == 0) {
            listPanel.add(new JLabel("No hay rutas activas para hoy."));
        } else {
            for (JsonElement e : rutasActivas) {
                listPanel.add(createRutaCard(e.getAsJsonObject()));
            }
        }
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null); scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsColumn(JsonObject progreso, JsonArray rutasCompletadas) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(350, 10)); // Ancho fijo
        panel.add(createProgresoCard(progreso));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createCompletadasCard(rutasCompletadas));
        panel.add(Box.createVerticalGlue()); // Empuja todo hacia arriba
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
        // TODO: Mostrar fecha real
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
        // TODO: Implementar menú de opciones (...)
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
        details.add(new JLabel(ruta.get("prioridad").getAsString())); // TODO: Colorear
        details.add(new JLabel("Estatus:"));
        details.add(new JLabel(ruta.get("estatus").getAsString())); // TODO: Colorear
        details.add(new JLabel("Creado el:"));
        details.add(new JLabel(ruta.get("creado").getAsString().substring(0, 10))); // Solo fecha
        card.add(details, BorderLayout.CENTER);
        return card;
    }

    // --- MÉTODOS COMPLETOS ---
    private JPanel createProgresoCard(JsonObject progreso) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.CENTER_ALIGNMENT); // Asegura alineación en BoxLayout
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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JPanel createCompletadasCard(JsonArray rutasCompletadas) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.CENTER_ALIGNMENT); // Asegura alineación en BoxLayout
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("Rutas completadas");
        title.setFont(UIConstants.TITLE.deriveFont(18f));
        header.add(title, BorderLayout.WEST);
        // TODO: Añadir icono y funcionalidad de búsqueda
        card.add(header, BorderLayout.NORTH);
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS)); listPanel.setOpaque(false);
        if (rutasCompletadas.size() == 0) {
            listPanel.add(new JLabel("Ninguna ruta completada hoy."));
        } else {
            for (JsonElement e : rutasCompletadas) {
                listPanel.add(createRutaCompletadaItem(e.getAsJsonObject()));
                listPanel.add(Box.createVerticalStrut(15)); // Espacio entre items
            }
            // Elimina el último espacio extra si hay elementos
            if (listPanel.getComponentCount() > 0) {
                listPanel.remove(listPanel.getComponentCount() - 1);
            }
        }
        card.add(listPanel, BorderLayout.CENTER);
        // Limita el tamaño máximo
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JPanel createRutaCompletadaItem(JsonObject ruta) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel(ruta.get("nombre").getAsString());
        title.setFont(UIConstants.TITLE.deriveFont(16f));
        item.add(title);
        String detailsText = String.format("ID: %s | Estatus: %s | Creado: %s",
                ruta.get("empId").getAsString(),
                ruta.get("estatus").getAsString(),
                ruta.get("creado").getAsString().substring(0, 10));
        JLabel details = new JLabel(detailsText);
        details.setFont(UIConstants.BASE.deriveFont(12f));
        details.setForeground(Color.GRAY);
        item.add(details);
        return item;
    }

    private void showCreateRutaDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Crear Nueva Ruta", true);
        try {
            JPanel formPanel = createRutaFormPanel(dialog);
            dialog.setContentPane(formPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (Exception e) { e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private JPanel createRutaFormPanel(JDialog dialog) throws Exception {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Nombre Ruta:"), gbc);
        gbc.gridx = 1;
        JTextField fNombre = new JTextField(20);
        panel.add(fNombre, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Prioridad:"), gbc);
        gbc.gridx = 1;
        String[] prioridades = {"Baja", "Media", "Alta"};
        JComboBox<String> fPrioridad = new JComboBox<>(prioridades);
        fPrioridad.setSelectedItem("Media");
        panel.add(fPrioridad, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Asignar a:"), gbc);
        gbc.gridx = 1;
        JComboBox<AdminDashboard.UserItem> fEmpleado = new JComboBox<>();

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/usuarios")).GET().build();
        HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            JsonArray usuarios = context.gson.fromJson(resp.body(), JsonArray.class);
            fEmpleado.addItem(new AdminDashboard.UserItem(0, "Sin Asignar", 100));
            for (JsonElement userElement : usuarios) {
                JsonObject usuario = userElement.getAsJsonObject();
                if (usuario.get("rol").getAsString().equals("empleado")) { fEmpleado.addItem(new AdminDashboard.UserItem(usuario.get("id").getAsInt(), usuario.get("nombre").getAsString(), usuario.get("tarifa_hora").getAsDouble())); }
            }
        } else { throw new Exception("Error al cargar empleados."); }
        panel.add(fEmpleado, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false); // Para que no tape el fondo
        RoundedButton cancelButton = new RoundedButton("Cancelar");
        cancelButton.addActionListener(e -> dialog.dispose());
        RoundedButton createButton = new RoundedButton("Crear Ruta");
        createButton.addActionListener(e -> {
            String nombre = fNombre.getText();
            String prioridad = (String) fPrioridad.getSelectedItem();
            AdminDashboard.UserItem empleado = (AdminDashboard.UserItem) fEmpleado.getSelectedItem();
            int empleadoId = (empleado != null) ? empleado.getId() : 0;
            if (nombre.isBlank()) { JOptionPane.showMessageDialog(dialog, "Nombre vacío.", "Error", JOptionPane.WARNING_MESSAGE); return; }
            JsonObject nuevaRuta = new JsonObject();
            nuevaRuta.addProperty("nombre", nombre);
            nuevaRuta.addProperty("prioridad", prioridad);
            if (empleadoId == 0) nuevaRuta.add("empleado_id", null);
            else nuevaRuta.addProperty("empleado_id", empleadoId);

            try {
                HttpRequest postReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/rutas/crear")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(context.gson.toJson(nuevaRuta))).build();
                HttpResponse<String> postResp = context.httpClient.send(postReq, HttpResponse.BodyHandlers.ofString());
                if (postResp.statusCode() == 201) {
                    JOptionPane.showMessageDialog(dialog, "Ruta creada.", "Éxito", JOptionPane.INFORMATION_MESSAGE); dialog.dispose();
                    // Refrescar la vista
                    JPanel newRutasView = new RutasViewPanel(context);
                    context.mainPanel.add(newRutasView, "RUTAS");
                    context.mainCards.show(context.mainPanel, "RUTAS");
                } else { throw new Exception(postResp.body()); }
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(dialog, "Error al crear:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });
        buttonPanel.add(cancelButton); buttonPanel.add(createButton); gbc.gridx = 1; gbc.gridy = 3; panel.add(buttonPanel, gbc);
        return panel;
    }
}