package org.paq.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.paq.AdminDashboard;
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HorariosViewPanel extends JPanel {

    private final AdminViewContext context;
    private final JTable scheduleTable;
    private final DefaultTableModel tableModel;
    private JComboBox<AdminDashboard.UserItem> fEmpleado;

    private static final String BASE_URL = "http://localhost:8081/api/admin";
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");

    // Variable para almacenar el ID del empleado seleccionado
    private Long selectedEmployeeId = null;

    public HorariosViewPanel(AdminViewContext context) {
        super(new BorderLayout(0, 20));
        this.context = context;

        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));

        // --- 1. Inicialización de la Tabla Semanal ---
        // Nombres iniciales. Se actualizarán dinámicamente en actualizarTablaHorarios()
        String[] columnNames = {"Empleado", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        scheduleTable = new JTable(tableModel);
        scheduleTable.setFont(UIConstants.BASE.deriveFont(14f).deriveFont(Font.BOLD));
        scheduleTable.setRowHeight(50);

        setupTableRenderer(scheduleTable);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // --- 2. Panel Superior ---
        JPanel topPanel = new JPanel(new BorderLayout(20, 0));
        topPanel.setOpaque(false);

        JPanel addHorarioPanel = createAddHorarioPanel();
        topPanel.add(addHorarioPanel, BorderLayout.CENTER);

        context.scheduleCalendar.setBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE));
        JPanel calendarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        calendarPanel.setOpaque(false);
        calendarPanel.add(context.scheduleCalendar);
        topPanel.add(calendarPanel, BorderLayout.EAST);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        // --- 3. Conector (Listener) ---
        context.scheduleCalendar.addPropertyChangeListener("calendar", evt -> {
            actualizarTablaHorarios();
        });

        // 4. Carga inicial
        cargarEmpleados();
        actualizarTablaHorarios();
    }

    // --- ESTILO Y RENDERIZADO DE LA TABLA ---
    private void setupTableRenderer(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(SwingConstants.CENTER);

                if (column > 0 && value != null) {
                    String text = value.toString();
                    if (text.contains("00:00 - 00:00 (Descanso)")) {
                        c.setBackground(new Color(255, 230, 230));
                        c.setForeground(new Color(150, 0, 0));
                    } else if (text.contains(" - ")) {
                        c.setBackground(new Color(230, 255, 230));
                        c.setForeground(new Color(0, 100, 0));
                    } else if (text.contains("Sin asignar")) {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.GRAY);
                    }
                    else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } else if (column == 0) {
                    c.setBackground(UIConstants.VERDE_700);
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // --- MÉTODOS DE VISTA Y LÓGICA ---

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

        fEmpleado = new JComboBox<>();
        fEmpleado.addItem(new AdminDashboard.UserItem(0, "Cargando Empleados...", 0.0));
        fEmpleado.setEnabled(false);

        fEmpleado.addActionListener(e -> {
            AdminDashboard.UserItem selected = (AdminDashboard.UserItem) fEmpleado.getSelectedItem();
            if (selected != null && selected.getId() > 0) {
                selectedEmployeeId = (long) selected.getId();
            } else {
                selectedEmployeeId = null;
            }
            actualizarTablaHorarios();
        });

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
        panel.add(new JLabel(""), gbc);
        return panel;
    }

    private void cargarEmpleados() {
        fEmpleado.removeAllItems();
        fEmpleado.addItem(new AdminDashboard.UserItem(0, "Cargando...", 0.0));

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/usuarios")).GET().build();

        context.httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    SwingUtilities.invokeLater(() -> {
                        fEmpleado.removeAllItems();
                        if (resp.statusCode() == 200) {
                            try {
                                JsonArray usuarios = context.gson.fromJson(resp.body(), JsonArray.class);
                                fEmpleado.addItem(new AdminDashboard.UserItem(0, "Seleccionar Empleado...", 0.0));
                                for (JsonElement userElement : usuarios) {
                                    JsonObject usuario = userElement.getAsJsonObject();

                                    String role = usuario.has("rol") ? usuario.get("rol").getAsString() : "";
                                    if (!"ADMIN".equalsIgnoreCase(role)) {
                                        fEmpleado.addItem(new AdminDashboard.UserItem(
                                                usuario.get("id").getAsInt(),
                                                usuario.get("nombre").getAsString(),
                                                usuario.has("tarifa_hora") && !usuario.get("tarifa_hora").isJsonNull()
                                                        ? usuario.get("tarifa_hora").getAsDouble() : 0.0
                                        ));
                                    }
                                }
                                fEmpleado.setEnabled(true);
                            } catch (Exception e) {
                                fEmpleado.addItem(new AdminDashboard.UserItem(-1, "Error de parseo.", 0.0));
                                fEmpleado.setEnabled(false);
                            }
                        } else {
                            fEmpleado.addItem(new AdminDashboard.UserItem(-1, "Error al cargar: " + resp.statusCode(), 0.0));
                            fEmpleado.setEnabled(false);
                        }
                    });
                })
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        fEmpleado.removeAllItems();
                        fEmpleado.addItem(new AdminDashboard.UserItem(-1, "Error de conexión.", 0.0));
                        fEmpleado.setEnabled(false);
                    });
                    return null;
                });
    }
    private void guardarHorario(JComboBox<AdminDashboard.UserItem> fEmpleado, String entrada, String salida, String estatus) {
        AdminDashboard.UserItem empleado = (AdminDashboard.UserItem) fEmpleado.getSelectedItem();
        Date fechaSeleccionada = context.scheduleCalendar.getDate();
        String fechaFormateada = new SimpleDateFormat("yyyy-MM-dd").format(fechaSeleccionada);

        if (empleado == null || empleado.getId() == 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Laboral".equals(estatus) && (!isValidTime(entrada) || !isValidTime(salida))) {
            JOptionPane.showMessageDialog(this, "Las horas deben estar en formato HH:mm (ej: 09:00).", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JsonObject payload = new JsonObject();
        // Conversión explícita a long (corrección de tipado)
        payload.addProperty("empleado_id", (long) empleado.getId());
        payload.addProperty("fecha", fechaFormateada);
        payload.addProperty("entrada", entrada);
        payload.addProperty("salida", salida);
        payload.addProperty("estatus", estatus);

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/horarios/crear"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(context.gson.toJson(payload)))
                    .build();

            context.httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        SwingUtilities.invokeLater(() -> {
                            if (resp.statusCode() == 201) {
                                JOptionPane.showMessageDialog(this, "Horario guardado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                                if (selectedEmployeeId != null && selectedEmployeeId.equals((long)empleado.getId())) {
                                    actualizarTablaHorarios();
                                }
                            } else {
                                // 💡 MEJORA: Capturamos el código de estado y el cuerpo del error
                                String errorBody = resp.body().isEmpty() ? "No hay detalles del error en la respuesta." : resp.body();

                                String errorMessage = String.format(
                                        "Error al guardar (HTTP %d):\n\nEl servidor respondió con:\n%s",
                                        resp.statusCode(),
                                        errorBody
                                );

                                JOptionPane.showMessageDialog(this, errorMessage, "Error del Servidor", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        SwingUtilities.invokeLater(() -> {
                            // 💡 MEJORA: Detalle en caso de fallo de conexión/red
                            String detailedError = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                            JOptionPane.showMessageDialog(this,
                                    "Fallo de conexión o red.\nAsegúrate de que el servidor (puerto 8081) esté corriendo.\nDetalle: " + detailedError,
                                    "Error de Conexión",
                                    JOptionPane.ERROR_MESSAGE
                            );
                            ex.printStackTrace();
                        });
                        return null;
                    });
        } catch (Exception ex) {
            // 💡 MEJORA: Error al construir la URL/Petición
            JOptionPane.showMessageDialog(this, "Error interno al construir la petición:\n" + ex.getMessage(), "Error Interno", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Actualiza la tabla de horarios SEMANAL del usuario seleccionado, de forma ASÍNCRONA.
     */
    private void actualizarTablaHorarios() {
        tableModel.setRowCount(0);

        if (selectedEmployeeId == null) {
            tableModel.addRow(new Object[]{"Selecciona un empleado para ver el horario semanal", "...", "...", "...", "...", "...", "...", "..."});
            return;
        }

        // --- 1. CÁLCULO DE FECHAS Y ACTUALIZACIÓN DEL HEADER ---
        Calendar cal = Calendar.getInstance();
        cal.setTime(context.scheduleCalendar.getDate());

        // 1. FORZAMOS el inicio de la semana a LUNES
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        // 2. Aplicamos la lógica para retroceder al Lunes de la semana actual (corrige el desfase regional)
        int today = cal.get(Calendar.DAY_OF_WEEK);
        if (today == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -6);
        } else {
            cal.add(Calendar.DAY_OF_YEAR, Calendar.MONDAY - today);
        }
        // 'cal' es ahora la fecha del Lunes correcto

        // Formateador para el encabezado (Ej: LUN 28)
        SimpleDateFormat headerDateFormat = new SimpleDateFormat("EEE d", new Locale("es", "ES"));
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String mondayDate = apiDateFormat.format(cal.getTime());

        List<String> newColumnNames = new ArrayList<>();
        newColumnNames.add("Empleado");

        // Clonamos para iterar y no modificar la fecha base para la llamada a la API
        Calendar headerCal = (Calendar) cal.clone();
        for (int i = 0; i < 7; i++) {
            newColumnNames.add(headerDateFormat.format(headerCal.getTime()).toUpperCase());
            headerCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Actualizar el header de la tabla y aplicar la carga en el EDT
        SwingUtilities.invokeLater(() -> {
            tableModel.setColumnIdentifiers(newColumnNames.toArray());
            setupTableRenderer(scheduleTable);

            // Mostrar mensaje de carga
            tableModel.addRow(new Object[]{fEmpleado.getSelectedItem().toString(), "Cargando...", "Cargando...", "Cargando...", "Cargando...", "Cargando...", "Cargando...", "Cargando..."});
        });

        // --- 2. LLAMADA ASÍNCRONA A LA API ---
        String endpoint = String.format("%s/horarios-semana/%d/%s",
                BASE_URL,
                selectedEmployeeId,
                mondayDate);

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(endpoint)).GET().build();

        context.httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0); // Limpiar "Cargando..."

                        if (resp.statusCode() == 200) {
                            try {
                                JsonArray horariosSemana = context.gson.fromJson(resp.body(), JsonArray.class);

                                List<Object> rowData = new ArrayList<>();
                                rowData.add(fEmpleado.getSelectedItem().toString());

                                for (JsonElement diaEl : horariosSemana) {
                                    if (diaEl.isJsonObject()) {
                                        JsonObject horarioDia = diaEl.getAsJsonObject();

                                        String estatus = horarioDia.get("estatus").getAsString();
                                        String entrada = horarioDia.get("entrada").getAsString();
                                        String salida = horarioDia.get("salida").getAsString();

                                        if ("Descanso".equalsIgnoreCase(estatus)) {
                                            rowData.add(String.format("%s - %s (Descanso)", entrada, salida));
                                        } else if ("Laboral".equalsIgnoreCase(estatus)) {
                                            rowData.add(String.format("%s - %s", entrada, salida));
                                        } else {
                                            rowData.add("Sin asignar");
                                        }
                                    } else {
                                        rowData.add("Sin asignar");
                                    }
                                }

                                tableModel.addRow(rowData.toArray());

                            } catch (Exception e) {
                                tableModel.addRow(new Object[]{"Error de parseo de datos", "...", "...", "...", "...", "...", "...", "..."});
                                e.printStackTrace();
                            }
                        } else {
                            tableModel.addRow(new Object[]{"Error al cargar horarios: " + resp.statusCode(), "...", "...", "...", "...", "...", "...", "..."});
                        }
                    });
                })
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        tableModel.addRow(new Object[]{"Error de conexión con el servidor", "...", "...", "...", "...", "...", "...", "..."});
                        ex.printStackTrace();
                    });
                    return null;
                });
    }

    private boolean isValidTime(String time) {
        if (time == null) return false;
        Matcher matcher = TIME_PATTERN.matcher(time);
        return matcher.matches();
    }
}