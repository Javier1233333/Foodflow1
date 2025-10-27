package org.paq.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.paq.AdminDashboard;
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddHorarioDialog extends JDialog {

    private final AdminViewContext context;
    // Callback para que HorariosViewPanel sepa cuándo refrescar la tabla
    private final Runnable refreshCallback;

    private JComboBox<AdminDashboard.UserItem> fEmpleado;
    private JTextField fEntrada;
    private JTextField fSalida;
    private JComboBox<String> fDia; // Para seleccionar el día/fecha

    public AddHorarioDialog(Frame owner, AdminViewContext context, Runnable refreshCallback) {
        super(owner, "Añadir Horario", true); // 'true' para hacerlo modal
        this.context = context;
        this.refreshCallback = refreshCallback;

        // Configuración de la ventana
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setBackground(Color.WHITE); // Fondo del diálogo

        // Panel principal con borde y un layout central
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIConstants.VERDE_700); // Fondo verde oscuro
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Título
        JLabel title = new JLabel("Añadir Horario");
        title.setFont(UIConstants.TITLE.deriveFont(28f).deriveFont(Font.BOLD));
        title.setForeground(Color.WHITE);

        // Inicializar componentes del formulario
        fEmpleado = new JComboBox<>();
        fEntrada = new JTextField("09:00", 5);
        fSalida = new JTextField("17:00", 5);

        // Combo box para el día (usa la fecha actual por defecto)
        fDia = new JComboBox<>();
        cargarDias(); // Llenar el combo de días (opcional, podrías usar un selector de fecha)

        // --- Cargar Empleados (Lógica movida/reusada) ---
        cargarEmpleados(fEmpleado);

        // Controles de Hora/Día con un estilo más "verde sobre blanco"
        JPanel formPanel = createFormPanel();

        // Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        RoundedButton btnGuardar = new RoundedButton("Confirmar");
        btnGuardar.setBackground(UIConstants.VERDE_700); // Color de acento
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> guardarHorario("Laboral"));

        RoundedButton btnDescanso = new RoundedButton("Marcar Descanso");
        btnDescanso.setBackground(UIConstants.BORDE_SUAVE.darker()); // Color diferente para descanso
        btnDescanso.setForeground(Color.WHITE);
        btnDescanso.addActionListener(e -> guardarHorario("Descanso"));

        // Puedes optar por un solo botón de 'Confirmar' y que el botón de 'Descanso' esté
        // en el panel principal (como en tu código original) o incluirlos aquí.
        buttonPanel.add(btnDescanso);
        buttonPanel.add(btnGuardar);

        // --- Ensamblaje del Panel Principal ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(title, gbc);

        // 2. Formulario (Empleado, Horas, Día)
        gbc.gridy = 1;
        mainPanel.add(formPanel, gbc);

        // 3. Botones
        gbc.gridy = 2;
        mainPanel.add(buttonPanel, gbc);

        this.getContentPane().add(mainPanel);
        this.pack(); // Ajustar el tamaño a los componentes
        this.setLocationRelativeTo(owner); // Centrar en la ventana principal
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Mantener el fondo del JDialog (verde oscuro)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiquetas y Campos

        // 1. Empleado
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblEmpleado = new JLabel("Empleado:");
        lblEmpleado.setForeground(Color.WHITE);
        panel.add(lblEmpleado, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(fEmpleado, gbc);

        // 2. Hora Inicio
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblInicio = new JLabel("Hora Inicio (HH:mm):");
        lblInicio.setForeground(Color.WHITE);
        panel.add(lblInicio, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(fEntrada, gbc);

        // 3. Hora Fin
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblFin = new JLabel("Hora Fin (HH:mm):");
        lblFin.setForeground(Color.WHITE);
        panel.add(lblFin, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(fSalida, gbc);

        // 4. Día
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblDia = new JLabel("Día:");
        lblDia.setForeground(Color.WHITE);
        panel.add(lblDia, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(fDia, gbc); // Usa el combo de días

        return panel;
    }

    // Método de carga de empleados (similar al original)
    private void cargarEmpleados(JComboBox<AdminDashboard.UserItem> fEmpleado) {
        // Limpia cualquier item anterior
        fEmpleado.removeAllItems();

        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/usuarios")).GET().build();
            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonArray usuarios = context.gson.fromJson(resp.body(), JsonArray.class);
                fEmpleado.addItem(new AdminDashboard.UserItem(0, "Seleccionar Empleado...", 0.0));
                for (JsonElement userElement : usuarios) {
                    JsonObject usuario = userElement.getAsJsonObject();
                    if (usuario.get("rol").getAsString().equals("empleado")) {
                        fEmpleado.addItem(new AdminDashboard.UserItem(
                                usuario.get("id").getAsInt(),
                                usuario.get("nombre").getAsString(),
                                usuario.get("tarifa_hora").getAsDouble()
                        ));
                    }
                }
            } else {
                fEmpleado.addItem(new AdminDashboard.UserItem(-1, "Error al cargar", 0.0));
            }
        } catch (Exception e) {
            fEmpleado.addItem(new AdminDashboard.UserItem(-1, "Error de conexión", 0.0));
            e.printStackTrace();
        }
    }

    // Método para llenar el ComboBox de días (opcional: podrías usar un selector JCalendar)
    private void cargarDias() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // Por defecto, añade solo la fecha actual
        fDia.addItem(dateFormat.format(new Date()));

        // Si quieres añadir un rango de fechas, podrías hacerlo aquí
    }

    // Método para guardar el horario (similar al original)
    private void guardarHorario(String estatus) {
        AdminDashboard.UserItem empleado = (AdminDashboard.UserItem) fEmpleado.getSelectedItem();
        String fechaFormateada = (String) fDia.getSelectedItem();

        String entrada = (estatus.equals("Descanso")) ? "00:00" : fEntrada.getText();
        String salida = (estatus.equals("Descanso")) ? "00:00" : fSalida.getText();

        if (empleado == null || empleado.getId() == 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: Validar formato HH:mm (omitido por brevedad, pero necesario)

        JsonObject payload = new JsonObject();
        payload.addProperty("empleado_id", empleado.getId());
        payload.addProperty("fecha", fechaFormateada);
        payload.addProperty("entrada", entrada);
        payload.addProperty("salida", salida);
        payload.addProperty("estatus", estatus);

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/admin/horarios/crear"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(context.gson.toJson(payload)))
                    .build();

            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 201) {
                JOptionPane.showMessageDialog(this, "Horario guardado para "  + " el " + fechaFormateada + ".", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                // Llama al callback para que el panel principal se actualice
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                this.dispose(); // Cierra el modal al tener éxito
            } else {
                throw new Exception(resp.body());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}