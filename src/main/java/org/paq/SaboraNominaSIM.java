package org.paq;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SaboraNominaSIM extends JFrame {

    // Tamaño mínimo y máximo (laptop)
    private static final int MIN_W = 960, MIN_H = 600;
    private static final int MAX_W = 1366, MAX_H = 800;

    private SidebarFoodFlow sidebar;
    private JPanel content;
    private JLabel logoTopRight;

    private PayrollPanel payrollPanel;

    public SaboraNominaSIM() {
        super("FoodFlow – Reparto");
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(1200, 720);
        setMinimumSize(new Dimension(MIN_W, MIN_H));

        // Sidebar (tu implementación existente)
        sidebar = new SidebarFoodFlow();
        add(sidebar);

        // Contenido
        content = new JPanel(null);
        content.setBackground(Color.WHITE);
        add(content);

        // Logo (opcional)
        logoTopRight = new JLabel();
        setFoodFlowLogo(logoTopRight, "/com/foodflow/assets/logo_foodflow.png", 180);
        content.add(logoTopRight);

        // Panel de Nómina (simulado)
        payrollPanel = new PayrollPanel();
        content.add(payrollPanel);

        relayout();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = Math.min(Math.max(getWidth(), MIN_W), MAX_W);
                int h = Math.min(Math.max(getHeight(), MIN_H), MAX_H);
                if (w != getWidth() || h != getHeight()) {
                    setSize(w, h);
                    return;
                }
                relayout();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);

        // Si tu SidebarFoodFlow permite registrar clicks:
        // sidebar.onClick("Pagos y Nóminas", this::showPayroll);
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

        sidebar.layoutSidebar(h);

        int left = sidebar.getFixedWidth();
        content.setBounds(left, 0, w - left, h);

        int logoW = logoTopRight.getPreferredSize().width;
        int logoH = logoTopRight.getPreferredSize().height;
        logoTopRight.setBounds(content.getWidth() - logoW - 24, 16, logoW, logoH);

        int panelW = Math.min(1000, content.getWidth() - 80);
        int panelH = Math.min(520, content.getHeight() - 120);
        int px = (content.getWidth() - panelW) / 2;
        int py = Math.max(logoH + 40, 70);
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
            target.setText("FoodFlow");
        }
    }

    // =================== SIMULADOR ===================

    // Empleado simulado
    static class Employee {
        long id;
        String name;
        double baseSalary;     // quincenal
        double hourlyRate;     // tarifa por hora
        double imssRate = 0.0725;
        double isrRate = 0.0862875;

        Employee(long id, String name, double baseSalary, double hourlyRate) {
            this.id = id;
            this.name = name;
            this.baseSalary = baseSalary;
            this.hourlyRate = hourlyRate;
        }

        @Override
        public String toString() {
            return id + " - " + name;
        }
    }

    // “BD” en memoria
    static class InMemoryDB {
        static final List<Employee> EMPLOYEES = List.of(
                new Employee(1, "Patricia Diaz", 8000.00, 100.00),
                new Employee(2, "Carlos Ruiz", 9500.00, 120.00),
                new Employee(3, "Ana López", 7600.00, 95.00)
        );
        static final List<String> PERIODS = List.of(
                "1era quincena de octubre", "2da quincena de octubre", "1era quincena de noviembre"
        );
        static final AtomicLong ID_GEN = new AtomicLong(1000);
    }

    // Calculadora
    static class PayrollCalc {
        static Result calculate(Employee e, double hours, double extraHours) {
            // Base quincenal directa + horas extra al doble
            double gross = e.baseSalary + (extraHours * e.hourlyRate * 2.0);
            double imss = round2(gross * e.imssRate);
            double isr = round2(gross * e.isrRate);
            double ded = round2(imss + isr);
            double net = round2(gross - ded);
            return new Result(round2(gross), imss, isr, ded, net);
        }

        static double round2(double v) {
            return Math.round(v * 100.0) / 100.0;
        }

        record Result(double gross, double imss, double isr, double deductions, double net) {
        }
    }

    // =================== PANEL DE NÓMINA ===================
    public class PayrollPanel extends JPanel {

        private final JTextField txtHoras  = new JTextField("8");
        private final JTextField txtExtra  = new JTextField("0");
        private final JTextField txtTarifa = new JTextField("100");   // tarifa fija por hora

        // Porcentajes editables
        private final JTextField txtIMSSpct = new JTextField("7.25");
        private final JTextField txtISRpct  = new JTextField("8.63");

        private final JComboBox<String> cbEmpleado = new JComboBox<>();
        private final JComboBox<String> cbPeriodo  = new JComboBox<>();
        private final JTextArea txtNotas = new JTextArea();

        private final JLabel lbIMSS = new JLabel("$0.00");
        private final JLabel lbISR  = new JLabel("$0.00");
        private final JLabel lbDed  = new JLabel("$0.00");
        private final JLabel lbNeto = new JLabel("$0.00");
        private final JLabel lbBruto= new JLabel("$0.00");

        private final JLabel lbHrsNorm  = new JLabel("0.0 hrs");
        private final JLabel lbHrsExtra = new JLabel("0.0 hrs");

        private final java.text.NumberFormat money = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es","MX"));

        public PayrollPanel() {
            setLayout(null);
            setBackground(Color.WHITE);

            JLabel titulo = new JLabel("Calculo nomina");
            titulo.setFont(new Font("Inter", Font.BOLD, 28));
            titulo.setBounds(60, 40, 320, 40);
            add(titulo);

            // Combos de demo
            cbEmpleado.addItem("1 - Patricia Diaz");
            cbEmpleado.addItem("2 - Carlos Ruiz");
            cbPeriodo.addItem("1era quincena de octubre");
            cbPeriodo.addItem("2da quincena de octubre");

            cbEmpleado.setBounds(60, 120, 360, 40);
            cbPeriodo.setBounds(60, 180, 360, 40);
            add(cbEmpleado);
            add(cbPeriodo);

            // Campos de captura a la derecha
            addLabeled(tf(txtHoras),  "Horas Trabajadas *", 480, 120, 180, 40);
            addLabeled(tf(txtExtra),  "Horas Extra",        700, 120, 180, 40);
            addLabeled(tf(txtTarifa), "Tarifa por hora $",  480, 180, 180, 40);

            // % IMSS e ISR
            addLabeled(tf(txtIMSSpct), "IMSS %", 700, 180, 85, 40);
            addLabeled(tf(txtISRpct),  "ISR %",  795, 180, 85, 40);

            // Notas
            txtNotas.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            JScrollPane sp = new JScrollPane(txtNotas);
            sp.setBounds(60, 240, 820, 40);
            add(sp);

            // Métricas
            int rowY = 300;
            int rowXY = 420;
            add(metric("Hrs. Normales", lbHrsNorm, 60, rowY));
            add(metric("Hrs. Extra",    lbHrsExtra,210, rowY));
            add(metric("Sueldo Bruto",  lbBruto,   360, rowY));
            add(metric("IMSS",          lbIMSS,    520, rowY));
            add(metric("ISR",           lbISR,     640, rowY));
            add(metric("Deducc.", lbDed,     740, rowY));

            add(metric("Sueldo Neto",   lbNeto,    740, rowXY));
            JButton btnGuardar = new JButton("Guardar");
            btnGuardar.setBackground(new Color(14,82,29));
            btnGuardar.setForeground(Color.WHITE);
            btnGuardar.setBounds(420, rowY + 80, 220, 44);
            add(btnGuardar);

            // Recalcular en vivo
            java.awt.event.KeyAdapter recalcKeys = new java.awt.event.KeyAdapter() {
                @Override public void keyReleased(java.awt.event.KeyEvent e) { calcular(); }
            };
            txtHoras.addKeyListener(recalcKeys);
            txtExtra.addKeyListener(recalcKeys);
            txtTarifa.addKeyListener(recalcKeys);
            txtIMSSpct.addKeyListener(recalcKeys);
            txtISRpct.addKeyListener(recalcKeys);

            java.awt.event.ActionListener recalc = ev -> calcular();
            cbEmpleado.addActionListener(recalc);
            cbPeriodo.addActionListener(recalc);

            btnGuardar.addActionListener(e -> guardar());

            // Setup inicial
            cbEmpleado.setSelectedIndex(0);
            cbPeriodo.setSelectedIndex(0);
            calcular();
        }

        private JTextField tf(JTextField t){
            t.setHorizontalAlignment(SwingConstants.RIGHT);
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

        private JPanel metric(String label, JLabel valueLabel, int x, int y){
            JPanel p = new JPanel(null);
            p.setBackground(Color.WHITE);
            p.setBounds(x, y, 140, 40);
            JLabel l = new JLabel(label, SwingConstants.CENTER);
            l.setBounds(0, 0, 140, 18);
            l.setForeground(new Color(110,110,110));
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
            valueLabel.setBounds(0, 18, 140, 20);
            if (label.equals("IMSS") || label.equals("ISR") || label.equals("Total Deducc."))
                valueLabel.setForeground(new Color(0,128,0));
            p.add(l); p.add(valueLabel);
            return p;
        }

        private void calcular() {
            try {
                double horas  = parseDouble(txtHoras.getText(), 0);
                double extra  = parseDouble(txtExtra.getText(), 0);
                double tarifa = parseDouble(txtTarifa.getText(), 0);

                double imssPct = parseDouble(txtIMSSpct.getText(), 0) / 100.0;
                double isrPct  = parseDouble(txtISRpct.getText(), 0) / 100.0;

                // Lógica pedida:
                double bruto = horas*tarifa + extra*tarifa*2.0;
                double imss  = bruto * imssPct;
                double isr   = (bruto - imss) * isrPct;
                double ded   = imss + isr;
                double neto  = bruto - ded;

                // Mostrar
                lbHrsNorm.setText(String.format(java.util.Locale.US, "%.1f hrs", horas));
                lbHrsExtra.setText(String.format(java.util.Locale.US, "%.1f hrs", extra));
                lbBruto.setText(money.format(r2(bruto)));
                lbIMSS.setText(money.format(r2(imss)));
                lbISR.setText(money.format(r2(isr)));
                lbDed.setText(money.format(r2(ded)));
                lbNeto.setText(money.format(r2(neto)));

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en cálculo: " + ex.getMessage());
            }
        }

        private void guardar() {
            // Simulación de guardado: solo muestra un ID ficticio y los datos actuales
            long id = System.currentTimeMillis() % 100000;
            JOptionPane.showMessageDialog(this,
                    "Nómina guardada (simulada)\n" +
                            "ID: " + id + "\n" +
                            "Empleado: " + cbEmpleado.getSelectedItem() + "\n" +
                            "Periodo: " + cbPeriodo.getSelectedItem());
        }

        private double parseDouble(String s, double def){
            try { return Double.parseDouble(s.trim()); } catch (Exception e){ return def; }
        }
        private double r2(double v){ return Math.round(v*100.0)/100.0; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SaboraNominaSIM::new);
    }
}
