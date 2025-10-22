package org.paq.views.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.paq.AdminDashboard; // Para UserItem
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class HorariosViewPanel extends JPanel {

    private final AdminViewContext context;
    private final DefaultTableModel tableModel; // Referencia a la tabla para refrescar

    public HorariosViewPanel(AdminViewContext context) {
        super(new BorderLayout(0, 20));
        this.context = context;

        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));

        // --- Tabla (Inferior) ---
        String[] columnNames = {"Empleado", "Hora de Entrada", "Hora de Salida"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable scheduleTable = new JTable(tableModel);
        scheduleTable.setFont(UIConstants.BASE.deriveFont(14f));
        scheduleTable.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // --- Panel Superior ---
        JPanel topPanel = new JPanel(new BorderLayout(20, 0));
        topPanel.setOpaque(false);

        // Formulario "Añadir Horario" (Izquierda-Superior)
        JPanel addHorarioPanel = createAddHorarioPanel(); // Ahora no necesita tableModel
        topPanel.add(addHorarioPanel, BorderLayout.CENTER);

        // Calendario JCalendar (Derecha-Superior)
        context.scheduleCalendar.setBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE));
        JPanel calendarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        calendarPanel.setOpaque(false);
        calendarPanel.add(context.scheduleCalendar); // Usa el del contexto
        topPanel.add(calendarPanel, BorderLayout.EAST);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        // --- Conector (Listener) ---
        context.scheduleCalendar.addPropertyChangeListener("calendar", evt -> {
            actualizarTablaHorarios(); // Llama al método de esta clase
        });

        // Carga inicial
        actualizarTablaHorarios();
    }

    // --- MÉTODOS DE AYUDA PARA HORARIOS ---

    // EN HorariosViewPanel.java
// REEMPLAZA este método

    private JPanel createAddHorarioPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel title = new JLabel("Añadir Horario");
        title.setFont(UIConstants.TITLE.deriveFont(24f));
        panel.add(title, gbc);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Empleado:"), gbc);
        gbc.gridx = 1;
        JComboBox<AdminDashboard.UserItem> fEmpleado = new JComboBox<>();
        try { // Carga de empleados
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/usuarios")).GET().build();
            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonArray usuarios = context.gson.fromJson(resp.body(), JsonArray.class);
                fEmpleado.addItem(new AdminDashboard.UserItem(0, "Seleccionar Empleado...", 0.0)); // Añade 0.0
                for (JsonElement userElement : usuarios) {
                    JsonObject usuario = userElement.getAsJsonObject();
                    if (usuario.get("rol").getAsString().equals("empleado")) {
                        fEmpleado.addItem(new AdminDashboard.UserItem(
                                usuario.get("id").getAsInt(),
                                usuario.get("nombre").getAsString(),
                                usuario.get("tarifa_hora").getAsDouble() // <-- Añade el 3er argumento
                        ));
                    }
                }
            } else {
                fEmpleado.addItem(new AdminDashboard.UserItem(-1, "Error al cargar", 0.0)); // Añade 0.0
            }
        } catch (Exception e) {
            fEmpleado.addItem(new AdminDashboard.UserItem(-1, "Error", 0.0)); // Añade 0.0
            e.printStackTrace(); // Es bueno ver el error
        }
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
        btnGuardar.addActionListener(e -> guardarHorario(fEmpleado, fEntrada.getText(), fSalida.getText(), "Laboral"));
        RoundedButton btnDescanso = new RoundedButton("Marcar Descanso");
        btnDescanso.addActionListener(e -> guardarHorario(fEmpleado, "00:00", "00:00", "Descanso"));
        buttonPanel.add(btnDescanso);
        buttonPanel.add(btnGuardar);
        panel.add(buttonPanel, gbc);
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        panel.add(new JLabel(""), gbc); // Espacio extra
        return panel;
    }

    private void guardarHorario(JComboBox<AdminDashboard.UserItem> fEmpleado, String entrada, String salida, String estatus) {
        AdminDashboard.UserItem empleado = (AdminDashboard.UserItem) fEmpleado.getSelectedItem();
        Date fechaSeleccionada = context.scheduleCalendar.getDate(); // Usa el calendario del contexto
        String fechaFormateada = new SimpleDateFormat("yyyy-MM-dd").format(fechaSeleccionada);
        if (empleado == null || empleado.getId() == 0) { JOptionPane.showMessageDialog(this, "Selecciona un empleado.", "Error", JOptionPane.WARNING_MESSAGE);
            return; }
        // TODO: Validar formato HH:mm
        JsonObject payload = new JsonObject();
        payload.addProperty("empleado_id", empleado.getId());
        payload.addProperty("fecha", fechaFormateada);
        payload.addProperty("entrada", entrada);
        payload.addProperty("salida", salida);
        payload.addProperty("estatus", estatus);
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/horarios/crear")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(context.gson.toJson(payload))).build();
            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 201) {
                JOptionPane.showMessageDialog(this, "Horario guardado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                actualizarTablaHorarios(); // Refresca la tabla de esta vista
            } else { throw new Exception(resp.body()); }
        } catch (Exception ex) { ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void actualizarTablaHorarios() {
        Date selectedDate = context.scheduleCalendar.getDate(); // Usa el calendario del contexto
        String fechaFormateada = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
        tableModel.setRowCount(0); // Usa la tabla de esta clase
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/horarios-dia/" + fechaFormateada)).GET().build();
            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonArray horarios = context.gson.fromJson(resp.body(), JsonArray.class);
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
}