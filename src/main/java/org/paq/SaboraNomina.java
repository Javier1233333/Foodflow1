package org.paq;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SaboraNomina extends JFrame {

    // Tamaño mínimo y máximo (laptop)
    private static final int MIN_W = 960, MIN_H = 600;
    private static final int MAX_W = 1366, MAX_H = 800;

    private SidebarFoodFlow sidebar;
    private JPanel content;
    private JLabel logoTopRight;

    // === NUEVO: referencia al panel de nómina ===
    private PayrollPanel payrollPanel;

    public SaboraNomina() {
        super("Sabora – Nomina");
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Tamaño inicial
        setSize(1200, 720);
        setMinimumSize(new Dimension(MIN_W, MIN_H));

        // Sidebar
        sidebar = new SidebarFoodFlow();
        add(sidebar);

        // Contenido
        content = new JPanel(null);
        content.setBackground(Color.WHITE);
        add(content);

        // Logo arriba a la derecha (desde resources)
        logoTopRight = new JLabel();
        // Si tu recurso existe, deja la línea; si no, comenta la siguiente:
        setFoodFlowLogo(logoTopRight, "/com/foodflow/assets/logo_foodflow.png", 180);
        content.add(logoTopRight);

        // === NUEVO: instanciar y agregar el panel de nómina ===
        payrollPanel = new PayrollPanel();
        content.add(payrollPanel);

        relayout();

        // Limitar crecimiento a “tamaño laptop” y bloquear ancho del sidebar
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                // Clamp a máximo
                int w = Math.min(getWidth(),  MAX_W);
                int h = Math.min(getHeight(), MAX_H);
                // Clamp a mínimo
                w = Math.max(w, MIN_W);
                h = Math.max(h, MIN_H);
                // Aplicar si cambió
                if (w != getWidth() || h != getHeight()) {
                    setSize(w, h);
                    return; // relayout se invoca de nuevo por el tamaño
                }
                relayout();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);

    }

    private void showPayroll() {
        content.removeAll();
        content.add(logoTopRight);
        content.add(payrollPanel);
        relayout();
        content.repaint();
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();

        // Sidebar mantiene ancho fijo
        sidebar.layoutSidebar(h);

        // Contenido ocupa el resto
        int left = sidebar.getFixedWidth();
        content.setBounds(left, 0, w - left, h);

        // Logo top-right (tamaño natural del PNG escalado)
        int logoW = logoTopRight.getPreferredSize().width;
        int logoH = logoTopRight.getPreferredSize().height;
        logoTopRight.setBounds(content.getWidth() - logoW - 24, 16, logoW, logoH);

        // === NUEVO: posiciona el panel de nómina de forma agradable dentro de content ===
        int panelW = Math.min(1000, content.getWidth() - 80);
        int panelH = Math.min(500, content.getHeight() - 120);
        int px = (content.getWidth() - panelW) / 2;
        int py = Math.max(logoH + 40, 80);
        payrollPanel.setBounds(px, py, panelW, panelH);
        payrollPanel.revalidate();
        payrollPanel.repaint();
    }

    private void setFoodFlowLogo(JLabel target, String resourcePath, int widthPx) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                int w = widthPx;
                int h = icon.getIconHeight() * w / icon.getIconWidth();
                Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                target.setIcon(new ImageIcon(scaled));
            } else {
                target.setText("FoodFlow");
                target.setFont(new Font("Inter", Font.BOLD, 28));
            }
        } catch (Exception ex) {
            target.setText("Sabora");
        }
    }

    public class PayrollPanel extends JPanel {

        private final JTextField txtHoras = new JTextField("8");
        private final JTextField txtExtra = new JTextField("0");
        private final JComboBox<String> cbEmpleado = new JComboBox<>();
        private final JComboBox<String> cbPeriodo = new JComboBox<>();
        private final JTextArea txtNotas = new JTextArea();

        private final JLabel lbIMSS = new JLabel("$0.00");
        private final JLabel lbISR  = new JLabel("$0.00");
        private final JLabel lbDed  = new JLabel("$0.00");
        private final JLabel lbNeto = new JLabel("$0.00");
        private final JLabel lbBruto= new JLabel("$0.00");

        public PayrollPanel() {
            setLayout(null);
            setBackground(Color.WHITE);

            // títulos y campos (coordenadas aproximadas según tu mock)
            JLabel titulo = new JLabel("Calculo nomina");
            titulo.setFont(new Font("Inter", Font.BOLD, 28));
            titulo.setBounds(60, 40, 300, 40);
            add(titulo);

            cbEmpleado.setBounds(60, 120, 360, 40);
            add(cbEmpleado);

            cbPeriodo.setBounds(60, 180, 360, 40);
            add(cbPeriodo);

            addLabeled(tf(txtHoras), "Horas Trabajadas *", 480, 120, 180, 40);
            addLabeled(tf(txtExtra), "Horas Extra",        700, 120, 180, 40);

            txtNotas.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            JScrollPane sp = new JScrollPane(txtNotas);
            sp.setBounds(480, 180, 400, 40);
            add(sp);

            int rowY = 260; // un poco más arriba porque el panel puede ser más bajito
            add(metric("Hrs. Normales", "8.0 hrs",  60, rowY));
            add(metric("Hrs. Extra",    "0.0 hrs", 210, rowY));
            add(metric("Sueldo Bruto",  lbBruto,   360, rowY));
            add(metric("IMSS",          lbIMSS,    520, rowY));
            add(metric("ISR",           lbISR,     640, rowY));
            add(metric("Total Deducc.", lbDed,     760, rowY));
            add(metric("Sueldo Neto",   lbNeto,    910, rowY));

            JButton btnGuardar = new JButton("Guardar");
            btnGuardar.setBackground(new Color(14,82,29));
            btnGuardar.setForeground(Color.WHITE);
            btnGuardar.setBounds(420, rowY + 80, 220, 44);
            add(btnGuardar);

            ActionListener calc = e -> calcular();
            txtHoras.addActionListener(calc);
            txtExtra.addActionListener(calc);
            cbEmpleado.addActionListener(calc);
            cbPeriodo.addActionListener(calc);

            btnGuardar.addActionListener(e -> guardar());

            // cálculo inicial
            calcular();
        }

        private JTextField tf(JTextField t){
            t.setBorder(BorderFactory.createLineBorder(new Color(210,210,210)));
            return t;
        }

        private void addLabeled(JComponent c, String label, int x, int y, int w, int h){
            JLabel l = new JLabel(label);
            l.setBounds(x, y-24, w, 20);
            add(l);
            c.setBounds(x, y, w, h);
            add(c);
        }

        private JPanel metric(String label, String value, int x, int y){
            JLabel v = new JLabel(value, SwingConstants.CENTER);
            return metric(label, v, x, y);
        }

        private JPanel metric(String label, JLabel valueLabel, int x, int y){
            JPanel p = new JPanel(null);
            p.setBackground(Color.WHITE);
            p.setBounds(x, y, 140, 40);
            JLabel l = new JLabel(label, SwingConstants.CENTER);
            l.setBounds(0, 0, 140, 18);
            l.setForeground(new Color(110,110,110));
            valueLabel.setBounds(0, 18, 140, 20);
            if (label.equals("IMSS") || label.equals("ISR") || label.equals("Total Deducc."))
                valueLabel.setForeground(new Color(0,128,0));
            p.add(l); p.add(valueLabel);
            return p;
        }

        private Long selectedEmployeeId(){
            String s = (String) cbEmpleado.getSelectedItem(); // "1 - Patricia Diaz"
            return (s!=null && s.contains(" - ")) ? Long.valueOf(s.split(" - ")[0]) : 1L;
        }

        private String selectedPeriod(){ return (String) cbPeriodo.getSelectedItem(); }

        private void calcular() {
            try {
                long empId = selectedEmployeeId();
                String period = selectedPeriod();
                double h = Double.parseDouble(txtHoras.getText());
                double ex = Double.parseDouble(txtExtra.getText());
                String body = """
                    {"employeeId": %d, "periodLabel": "%s", "overrideHours": %.2f, "overrideExtra": %.2f, "notes": %s}
                """.formatted(empId, period, h, ex,
                        (txtNotas.getText().isBlank() ? "null" : "\"" + txtNotas.getText().replace("\"","\\\"") + "\""));

                HttpResponse<String> resp = httpPost("http://localhost:8083/payroll/calculate", body);
                Map<String,Object> json = parseJson(resp.body());
                lbBruto.setText(asMoney((Double)json.getOrDefault("gross", 0.0)));
                lbIMSS.setText(asMoney((Double)json.getOrDefault("imss", 0.0)));
                lbISR.setText(asMoney((Double)json.getOrDefault("isr", 0.0)));
                lbDed.setText(asMoney((Double)json.getOrDefault("deductionsTotal", 0.0)));
                lbNeto.setText(asMoney((Double)json.getOrDefault("net", 0.0)));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al calcular: " + ex.getMessage());
            }
        }

        private void guardar() {
            try {
                long empId = selectedEmployeeId();
                String period = selectedPeriod();
                double h = Double.parseDouble(txtHoras.getText());
                double ex = Double.parseDouble(txtExtra.getText());
                String body = """
                    {"employeeId": %d, "periodLabel": "%s", "overrideHours": %.2f, "overrideExtra": %.2f, "notes": %s}
                """.formatted(empId, period, h, ex,
                        (txtNotas.getText().isBlank() ? "null" : "\"" + txtNotas.getText().replace("\"","\\\"") + "\""));

                HttpResponse<String> resp = httpPost("http://localhost:8083/payroll/save", body);
                Map<String,Object> json = parseJson(resp.body());
                JOptionPane.showMessageDialog(this, "Guardado con ID: " + json.get("savedId"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            }
        }

        // --- util HTTP (Java 11+)
        private HttpResponse<String> httpPost(String url, String json) throws Exception {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            var req = java.net.http.HttpRequest.newBuilder(new java.net.URI(url))
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type","application/json").build();
            return client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
        }

        // --- parseo simple sin librerías: extrae dobles por clave del JSON
        private Map<String,Object> parseJson(String s){
            Map<String,Object> out = new HashMap<>();
            out.put("gross",            extractDouble(s, "\"gross\"\\s*:\\s*([0-9.\\-E]+)"));
            out.put("imss",             extractDouble(s, "\"imss\"\\s*:\\s*([0-9.\\-E]+)"));
            out.put("isr",              extractDouble(s, "\"isr\"\\s*:\\s*([0-9.\\-E]+)"));
            out.put("deductionsTotal",  extractDouble(s, "\"deductionsTotal\"\\s*:\\s*([0-9.\\-E]+)"));
            out.put("net",              extractDouble(s, "\"net\"\\s*:\\s*([0-9.\\-E]+)"));
            // si necesitas más campos, agrégalos aquí
            return out;
        }

        private double extractDouble(String json, String regex){
            try {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(json);
                if (m.find()) return Double.parseDouble(m.group(1));
            } catch (Exception ignored) {}
            return 0.0;
        }

        private String asMoney(double v){
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es","MX"));
            return nf.format(v);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SaboraNomina::new);
    }
}
