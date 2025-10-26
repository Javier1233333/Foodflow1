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
    private final JComboBox<String> cbPeriodo = new JComboBox<>(new String[]{"Seleccionar Periodo..."}); // Llenar después
    private final JTextField txtHoras = createNumericField("80"); // Horas quincenales
    private final JTextField txtExtra = createNumericField("0");
    private final JTextField txtTarifaBase = createNumericField("0.00"); // Se cargará del empleado
    private final JTextField txtIMSSpct = createNumericField("7.25");
    private final JTextField txtISRpct = createNumericField("8.63");
    private final JTextArea txtNotas = new JTextArea();

    private final JLabel lbBruto = createResultLabel();
    private final JLabel lbIMSS = createResultLabel();
    private final JLabel lbISR = createResultLabel();
    private final JLabel lbDed = createResultLabel();
    private final JLabel lbNeto = createResultLabel();

    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    // --- Datos Simulados/Cargados ---
    // Usaremos UserItem de AdminDashboard que ya carga usuarios
    // Podríamos crear una clase NominaPeriodo si fuera necesario

    public NominasViewPanel(AdminViewContext context) {
        super(new GridBagLayout()); // Usar GridBagLayout
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 20, 20, 20)); // Añadir márgenes
        initComponents();
        loadInitialData();
        setupListeners();
        calcular(); // Calcular al inicio
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Espaciado
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Fila 0: Título ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4; // Ocupa 4 columnas
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JLabel titulo = new JLabel("Cálculo de Nómina");
        add(titulo, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Restaurar fill
        gbc.anchor = GridBagConstraints.WEST;    // Restaurar anchor

        // --- Fila 1: Selección Empleado y Periodo ---
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(new JLabel("Empleado:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        add(cbEmpleado, gbc); // Más ancho

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(new JLabel("Periodo:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        add(cbPeriodo, gbc);

        // --- Fila 3: Horas y Tarifas ---
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(new JLabel("Horas Trabajadas:"), gbc);
        gbc.gridx = 1;
        add(txtHoras, gbc);
        gbc.gridx = 2;
        add(new JLabel("Horas Extra:"), gbc);
        gbc.gridx = 3;
        add(txtExtra, gbc);

        // --- Fila 4: Porcentajes ---
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(new JLabel("Tarifa Base ($/hr):"), gbc); // Tarifa base por hora
        gbc.gridx = 1;
        add(txtTarifaBase, gbc);
        txtTarifaBase.setEditable(false); // La tarifa viene del empleado
        txtTarifaBase.setBackground(Color.lightGray);

        // Dejamos espacio o ponemos los % aquí
        gbc.gridx = 2;
        add(new JLabel("IMSS (%):"), gbc);
        gbc.gridx = 3;
        add(txtIMSSpct, gbc);


        // --- Fila 5: % ISR ---
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.gridx = 2;
        add(new JLabel("ISR (%):"), gbc);
        gbc.gridx = 3;
        add(txtISRpct, gbc);

        // --- Fila 6: Notas ---
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(new JLabel("Notas:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.ipady = 40; // Alto para el área de texto
        txtNotas.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(new JScrollPane(txtNotas), gbc);
        gbc.ipady = 0; // Restaurar alto

        // --- Fila 7: Resultados ---
        gbc.gridy = 7;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(20, 0, 10, 0); // Más espacio arriba
        add(createResultsPanel(), gbc);
        gbc.insets = new Insets(5, 10, 5, 10); // Restaurar insets

        // --- Fila 8: Botón Guardar ---
        gbc.gridy = 8;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        RoundedButton btnGuardar = new RoundedButton("Guardar Nómina");
        btnGuardar.setPreferredSize(new Dimension(250, 40));
        btnGuardar.addActionListener(e -> guardar());
        add(btnGuardar, gbc);

        // --- Espacio extra al final ---
        gbc.gridy = 9;
        gbc.weighty = 1.0;
        add(new JLabel(" "), gbc); // Para empujar todo hacia arriba
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 0)); // 1 Fila, 5 Columnas
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Resultados del Cálculo"));

        panel.add(createMetricPanel("Sueldo Bruto", lbBruto));
        panel.add(createMetricPanel("IMSS", lbIMSS, Color.RED));
        panel.add(createMetricPanel("ISR", lbISR, Color.RED));
        panel.add(createMetricPanel("Total Deducc.", lbDed, Color.RED));
        panel.add(createMetricPanel("Sueldo Neto", lbNeto, UIConstants.VERDE_800)); // Verde oscuro

        return panel;
    }
    // EN NominasViewPanel.java

    private void loadInitialData() {
        cbEmpleado.removeAllItems(); // Limpia antes de añadir
        cbEmpleado.addItem(new AdminDashboard.UserItem(0, "Seleccionar Empleado...", 0.0)); // Item por defecto
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/usuarios")).GET().build();
            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonArray usuarios = context.gson.fromJson(resp.body(), JsonArray.class);
                for (JsonElement userElement : usuarios) {
                    JsonObject usuario = userElement.getAsJsonObject();
                    if (usuario.get("rol").getAsString().equals("empleado")) {
                        cbEmpleado.addItem(new AdminDashboard.UserItem(
                                usuario.get("id").getAsInt(),
                                usuario.get("nombre").getAsString(),
                                usuario.get("tarifa_hora").getAsDouble()
                        ));
                    }
                }
            } else { System.err.println("Error cargando empleados: " + resp.body()); }
        } catch (Exception e) { e.printStackTrace(); }

        // Cargar Periodos (simulado) - Podrías cargar esto desde BD también
        cbPeriodo.removeAllItems(); // Limpia
        cbPeriodo.addItem("Seleccionar Periodo...");
        cbPeriodo.addItem("1era Quincena Octubre 2025");
        cbPeriodo.addItem("2da Quincena Octubre 2025");
        cbPeriodo.addItem("1era Quincena Noviembre 2025");
        // ... (añadir más periodos si es necesario)
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
                // Actualiza la tarifa base cuando cambia el empleado
                txtTarifaBase.setText(String.format(Locale.US, "%.2f", selected.getTarifaHora()));
            } else {
                txtTarifaBase.setText("0.00"); // Limpia si no hay selección
            }
            calcular(); // Recalcula con la nueva tarifa
        });
        cbPeriodo.addActionListener(e -> calcular()); // Recalcular si cambia el periodo (aunque no afecte cálculo base)
    }
    // EN NominasViewPanel.java
    private void guardar() {
        AdminDashboard.UserItem empleado = (AdminDashboard.UserItem) cbEmpleado.getSelectedItem();
        String periodo = (String) cbPeriodo.getSelectedItem();

        if (empleado == null || empleado.getId() == 0 || periodo == null || periodo.equals("Seleccionar Periodo...")) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado y un periodo válidos.", "Faltan Datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Recalcula una última vez para asegurar datos frescos
            calcular();

            // 1. Recolectar todos los datos necesarios
            JsonObject nominaData = new JsonObject();
            nominaData.addProperty("empleado_id", empleado.getId());
            nominaData.addProperty("periodo", periodo);
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
                    .uri(URI.create("http://localhost:8080/api/admin/nominas/guardar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(context.gson.toJson(nominaData)))
                    .build();

            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            // 3. Mostrar resultado
            if (resp.statusCode() == 201) {
                JOptionPane.showMessageDialog(this, "Nómina guardada/actualizada con éxito.", "Guardado Exitoso", JOptionPane.INFORMATION_MESSAGE);
                // Opcional: Limpiar campos o cargar siguiente empleado/periodo
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
        return createMetricPanel(title, valueLabel, Color.BLACK); // Color por defecto
    }

    private JPanel createMetricPanel(String title, JLabel valueLabel, Color valueColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.GRAY);
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(UIConstants.BASE.deriveFont(Font.BOLD, 16f)); // Más grande
        p.add(titleLabel, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private JTextField createNumericField(String defaultValue) {
        JTextField tf = new JTextField(defaultValue);
        tf.setHorizontalAlignment(SwingConstants.RIGHT);
        // Podríamos añadir validación para que solo acepte números
        return tf;
    }
    private JLabel createResultLabel() {
        JLabel lbl = new JLabel("$0.00");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    // --- Métodos de Cálculo y Ayuda (Faltaban) ---

    private void calcular() {
        String zeroMoney = moneyFormat.format(0.0); // Formato $0.00
        try {
            System.out.println("--- Iniciando cálculo ---"); // DEBUG

            double horas = parseDouble(txtHoras.getText(), 0);
            double extra = parseDouble(txtExtra.getText(), 0);
            double tarifaBase = parseDouble(txtTarifaBase.getText(), 0);
            double imssPct = parseDouble(txtIMSSpct.getText(), 0) / 100.0;
            double isrPct = parseDouble(txtISRpct.getText(), 0) / 100.0;

            // --- Imprime los valores leídos ---
            System.out.println("Horas: " + horas);
            System.out.println("Extra: " + extra);
            System.out.println("Tarifa Base: " + tarifaBase);
            System.out.println("IMSS %: " + imssPct);
            System.out.println("ISR %: " + isrPct);
            // ------------------------------------

            // Lógica de cálculo
            double bruto = (horas * tarifaBase) + (extra * tarifaBase * 2.0);
            double imss = bruto * imssPct;
            double isr = (bruto - imss) * isrPct;
            double ded = imss + isr;
            double neto = bruto - ded;

            // --- Imprime los valores calculados ---
            System.out.println("Bruto Calculado: " + bruto);
            System.out.println("IMSS Calculado: " + imss);
            System.out.println("ISR Calculado: " + isr);
            System.out.println("Deducciones: " + ded);
            System.out.println("Neto Calculado: " + neto);
            System.out.println("--------------------------");
            // --------------------------------------

            // Actualizar JLabels (con chequeo por si acaso)
            if (lbBruto != null) lbBruto.setText(moneyFormat.format(round2(bruto))); else System.err.println("lbBruto es NULL");
            if (lbIMSS != null) lbIMSS.setText(moneyFormat.format(round2(imss))); else System.err.println("lbIMSS es NULL");
            if (lbISR != null) lbISR.setText(moneyFormat.format(round2(isr))); else System.err.println("lbISR es NULL");
            if (lbDed != null) lbDed.setText(moneyFormat.format(round2(ded))); else System.err.println("lbDed es NULL");
            if (lbNeto != null) lbNeto.setText(moneyFormat.format(round2(neto))); else System.err.println("lbNeto es NULL");

        } catch (NumberFormatException ex) {
            System.err.println("Error de formato numérico: " + ex.getMessage()); // DEBUG
            // Resetear labels en caso de error de formato
            if (lbBruto != null) lbBruto.setText("Error Formato");
            if (lbIMSS != null) lbIMSS.setText(zeroMoney);
            if (lbISR != null) lbISR.setText(zeroMoney);
            if (lbDed != null) lbDed.setText(zeroMoney);
            if (lbNeto != null) lbNeto.setText(zeroMoney);
        } catch (Exception e) {
            // Capturar cualquier otro error inesperado
            e.printStackTrace(); // Imprime la traza completa del error
            if (lbBruto != null) lbBruto.setText("Error Calc.");
            if (lbIMSS != null) lbIMSS.setText(zeroMoney);
            if (lbISR != null) lbISR.setText(zeroMoney);
            if (lbDed != null) lbDed.setText(zeroMoney);
            if (lbNeto != null) lbNeto.setText(zeroMoney);
        }
    }

    private double parseDouble(String s, double defaultValue) {
        try {
            // Reemplaza comas por puntos si se usan como decimal y quita otros caracteres no numéricos excepto el punto
            String cleaned = s.trim().replace(',', '.').replaceAll("[^\\d.]", "");
            // Evita múltiples puntos decimales
            if (cleaned.indexOf('.') != cleaned.lastIndexOf('.')) {
                throw new NumberFormatException("Múltiples puntos decimales");
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