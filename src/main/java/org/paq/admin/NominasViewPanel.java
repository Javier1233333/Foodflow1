package org.paq.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.paq.AdminDashboard;
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.util.Locale;

public class NominasViewPanel extends JPanel {

    private final AdminViewContext context;

    // --- Componentes de la UI ---
    private final JComboBox<AdminDashboard.UserItem> cbEmpleado = new JComboBox<>();
    private final JComboBox<String> cbPeriodo = new JComboBox<>(new String[]{"Seleccionar Periodo..."});
    private final JTextField txtHoras = createNumericField("80");
    private final JTextField txtExtra = createNumericField("0");
    private final JTextField txtTarifaBase = createNumericField("0.00");
    private final JTextField txtIMSSpct = createNumericField("7.25");
    private final JTextField txtISRpct = createNumericField("8.63");
    private final JTextArea txtNotas = new JTextArea();

    private final JLabel lbBruto = createResultLabel();
    private final JLabel lbIMSS = createResultLabel();
    private final JLabel lbISR = createResultLabel();
    private final JLabel lbDed = createResultLabel();
    private final JLabel lbNeto = createResultLabel();

    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public NominasViewPanel(AdminViewContext context) {
        super(new GridBagLayout());
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
        loadInitialData();
        setupListeners();
        calcular();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Fila 0: Título ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        JLabel titulo = new JLabel("Cálculo de Nómina");
        add(titulo, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;

        // --- Fila 1: Empleado ---
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.gridx = 0; add(new JLabel("Empleado:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; add(cbEmpleado, gbc);

        // --- Fila 2: Periodo ---
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.gridx = 0; add(new JLabel("Periodo:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; add(cbPeriodo, gbc);

        // --- Fila 3: Horas ---
        gbc.gridy = 3; gbc.gridwidth = 1; gbc.gridx = 0; add(new JLabel("Horas Trabajadas:"), gbc);
        gbc.gridx = 1; add(txtHoras, gbc);
        gbc.gridx = 2; add(new JLabel("Horas Extra:"), gbc);
        gbc.gridx = 3; add(txtExtra, gbc);

        // --- Fila 4: Tarifa y IMSS ---
        gbc.gridy = 4; gbc.gridwidth = 1; gbc.gridx = 0; add(new JLabel("Tarifa Base ($/hr):"), gbc);
        gbc.gridx = 1; add(txtTarifaBase, gbc);
        txtTarifaBase.setEditable(false); txtTarifaBase.setBackground(Color.lightGray);
        gbc.gridx = 2; add(new JLabel("IMSS (%):"), gbc);
        gbc.gridx = 3; add(txtIMSSpct, gbc);

        // --- Fila 5: ISR ---
        gbc.gridy = 5; gbc.gridwidth = 1; gbc.gridx = 2; add(new JLabel("ISR (%):"), gbc);
        gbc.gridx = 3; add(txtISRpct, gbc);

        // --- Fila 6: Notas ---
        gbc.gridy = 6; gbc.gridwidth = 1; gbc.gridx = 0; add(new JLabel("Notas:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.ipady = 40;
        txtNotas.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(new JScrollPane(txtNotas), gbc);
        gbc.ipady = 0;

        // --- Fila 7: Resultados ---
        gbc.gridy = 7; gbc.gridwidth = 4; gbc.insets = new Insets(20, 0, 10, 0);
        add(createResultsPanel(), gbc);
        gbc.insets = new Insets(5, 10, 5, 10);

        // --- Fila 8: Botón Guardar y Ver Historial ---
        gbc.gridy = 8; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonRow.setOpaque(false);

        RoundedButton btnGuardar = new RoundedButton("Guardar Nómina");
        btnGuardar.setPreferredSize(new Dimension(200, 40));
        btnGuardar.addActionListener(e -> guardar());
        buttonRow.add(btnGuardar);

        RoundedButton btnVerTabla = new RoundedButton("Ver Historial de Nóminas");
        btnVerTabla.setPreferredSize(new Dimension(250, 40));
        btnVerTabla.setBackground(UIConstants.VERDE_800.darker());
        btnVerTabla.addActionListener(e -> showNominasHistoryTable());
        buttonRow.add(btnVerTabla);

        add(buttonRow, gbc);

        // --- Espacio extra ---
        gbc.gridy = 10; gbc.weighty = 1.0; add(new JLabel(" "), gbc);
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Resultados del Cálculo"));

        panel.add(createMetricPanel("Sueldo Bruto", lbBruto));
        panel.add(createMetricPanel("IMSS", lbIMSS, Color.RED));
        panel.add(createMetricPanel("ISR", lbISR, Color.RED));
        panel.add(createMetricPanel("Total Deducc.", lbDed, Color.RED));
        panel.add(createMetricPanel("Sueldo Neto", lbNeto, UIConstants.VERDE_800));

        return panel;
    }

    private void loadInitialData() {
        cbEmpleado.removeAllItems();
        cbEmpleado.addItem(new AdminDashboard.UserItem(0, "Seleccionar Empleado...", 0.0));

        // Carga de empleados (usa SwingWorker para no bloquear la UI)
        new SwingWorker<HttpResponse<String>, Void>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                // CORRECCIÓN DE PUERTO: 8081
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8081/api/admin/usuarios")).GET().build();
                return context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            }

            @Override
            protected void done() {
                try {
                    HttpResponse<String> resp = get();
                    if (resp.statusCode() == 200) {
                        JsonArray usuarios = context.gson.fromJson(resp.body(), JsonArray.class);
                        for (JsonElement userElement : usuarios) {
                            JsonObject usuario = userElement.getAsJsonObject();
                            String role = usuario.get("rol").getAsString();
                            if ("USER".equals(role) || "ADMIN".equals(role)) {
                                cbEmpleado.addItem(new AdminDashboard.UserItem(
                                        usuario.get("id").getAsInt(),
                                        usuario.get("nombre").getAsString(),
                                        usuario.get("tarifa_hora").getAsDouble()
                                ));
                            }
                        }
                    } else { System.err.println("Error cargando empleados: " + resp.body()); }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();


        // Cargar Periodos (simulado - ya usa el formato numérico)
        cbPeriodo.removeAllItems();
        cbPeriodo.addItem("Seleccionar Periodo...");
        cbPeriodo.addItem("202501"); // Ene 1ra
        cbPeriodo.addItem("202502"); // Ene 2da
        cbPeriodo.addItem("202503"); // Feb 1ra
    }

    private void setupListeners() {
        KeyAdapter recalcKeyListener = new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { calcular(); }
        };
        txtHoras.addKeyListener(recalcKeyListener);
        txtExtra.addKeyListener(recalcKeyListener);
        txtIMSSpct.addKeyListener(recalcKeyListener);
        txtISRpct.addKeyListener(recalcKeyListener);

        cbEmpleado.addActionListener(e -> {
            AdminDashboard.UserItem selected = (AdminDashboard.UserItem) cbEmpleado.getSelectedItem();
            if (selected != null && selected.getId() != 0) {
                txtTarifaBase.setText(String.format(Locale.US, "%.2f", selected.getTarifaHora()));
            } else {
                txtTarifaBase.setText("0.00");
            }
            calcular();
        });
        cbPeriodo.addActionListener(e -> calcular());
    }

    private void showNominasHistoryTable() {
        NominasHistoryViewPanel historyPanel = new NominasHistoryViewPanel(context);

        JFrame frame = new JFrame("Historial de Nóminas Guardadas");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(historyPanel);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        frame.setVisible(true);
    }

    private void guardar() {
        AdminDashboard.UserItem empleado = (AdminDashboard.UserItem) cbEmpleado.getSelectedItem();
        String periodoIDString = (String) cbPeriodo.getSelectedItem(); // Captura el String numérico (ej: "202501")

        // 1. VALIDACIÓN DEL CLIENTE: Verifica que no sean los placeholders
        if (empleado == null || empleado.getId() == 0 || periodoIDString == null || periodoIDString.equals("Seleccionar Periodo...")) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado y un periodo válidos.", "Faltan Datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer periodIdNumerico;
        try {
            // CONVERSIÓN CRÍTICA: Convierte el String ID numérico (ej: "202501") a Integer
            periodIdNumerico = Integer.parseInt(periodoIDString);

            // VALIDACIÓN ADICIONAL: Ya cubierta por la verificación de "Seleccionar Periodo..."
            // if (periodIdNumerico == 0) throw new NumberFormatException(); // Esto podría ser un problema si el valor 0 fuera válido

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: El ID del Periodo es inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        try {
            calcular();

            // 1. Recolectar datos y construir el JSON
            JsonObject nominaData = new JsonObject();
            nominaData.addProperty("empleado_id", (long)empleado.getId());
            nominaData.addProperty("periodoID", periodIdNumerico);

            nominaData.addProperty("horas_normales", parseDouble(txtHoras.getText(), 0));
            nominaData.addProperty("horas_extra", parseDouble(txtExtra.getText(), 0));
            nominaData.addProperty("tarifa_base", parseDouble(txtTarifaBase.getText(), 0));

            // Obtener valores calculados (quitando el '$' y comas)
            nominaData.addProperty("sueldo_bruto", parseDouble(lbBruto.getText().replaceAll("[$,]", ""), 0));
            nominaData.addProperty("imss", parseDouble(lbIMSS.getText().replaceAll("[$,]", ""), 0));
            nominaData.addProperty("isr", parseDouble(lbISR.getText().replaceAll("[$,]", ""), 0));
            nominaData.addProperty("deducciones", parseDouble(lbDed.getText().replaceAll("[$,]", ""), 0));
            nominaData.addProperty("sueldo_neto", parseDouble(lbNeto.getText().replaceAll("[$,]", ""), 0));
            nominaData.addProperty("notas", txtNotas.getText());

            // 2. Enviar al backend
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/admin/nominas/guardar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(context.gson.toJson(nominaData)))
                    .build();

            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            // 3. Mostrar resultado
            if (resp.statusCode() == 201 || resp.statusCode() == 200) {
                JOptionPane.showMessageDialog(this, "Nómina guardada/actualizada con éxito.", "Guardado Exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JsonObject errorJson = context.gson.fromJson(resp.body(), JsonObject.class);
                String errorMsg = errorJson.has("error") ? errorJson.get("error").getAsString() : "Error desconocido";
                throw new Exception("Error del servidor (" + resp.statusCode() + "): " + errorMsg);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar la nómina:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createMetricPanel(String title, JLabel valueLabel) {
        return createMetricPanel(title, valueLabel, Color.BLACK);
    }

    private JPanel createMetricPanel(String title, JLabel valueLabel, Color valueColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.GRAY);
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(UIConstants.BASE.deriveFont(Font.BOLD, 16f));
        p.add(titleLabel, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private JTextField createNumericField(String defaultValue) {
        JTextField tf = new JTextField(defaultValue);
        tf.setHorizontalAlignment(SwingConstants.RIGHT);
        return tf;
    }
    private JLabel createResultLabel() {
        JLabel lbl = new JLabel("$0.00");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private void calcular() {
        String zeroMoney = moneyFormat.format(0.0);
        try {
            double horas = parseDouble(txtHoras.getText(), 0);
            double extra = parseDouble(txtExtra.getText(), 0);
            double tarifaBase = parseDouble(txtTarifaBase.getText(), 0);
            double imssPct = parseDouble(txtIMSSpct.getText(), 0) / 100.0;
            double isrPct = parseDouble(txtISRpct.getText(), 0) / 100.0;

            double bruto = (horas * tarifaBase) + (extra * tarifaBase * 2.0);
            double imss = bruto * imssPct;
            double isr = (bruto - imss) * isrPct;
            double ded = imss + isr;
            double neto = bruto - ded;

            if (lbBruto != null) lbBruto.setText(moneyFormat.format(round2(bruto)));
            if (lbIMSS != null) lbIMSS.setText(moneyFormat.format(round2(imss)));
            if (lbISR != null) lbISR.setText(moneyFormat.format(round2(isr)));
            if (lbDed != null) lbDed.setText(moneyFormat.format(round2(ded)));
            if (lbNeto != null) lbNeto.setText(moneyFormat.format(round2(neto)));

        } catch (NumberFormatException ex) {
            if (lbBruto != null) lbBruto.setText("Error Formato");
            if (lbIMSS != null) lbIMSS.setText(zeroMoney);
            if (lbISR != null) lbISR.setText(zeroMoney);
            if (lbDed != null) lbDed.setText(zeroMoney);
            if (lbNeto != null) lbNeto.setText(zeroMoney);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double parseDouble(String s, double defaultValue) {
        try {
            String cleaned = s.trim().replace(',', '.').replaceAll("[^\\d.]", "");
            if (cleaned.indexOf('.') != cleaned.lastIndexOf('.')) {
                return defaultValue;
            }
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}