package org.paq;

import org.paq.UI.IconLabel;
import org.paq.UI.LinkLabel;
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
// Imports necesarios para la conexión
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class FoodFlowLogin extends JFrame {

    /**
     * Maneja la lógica de inicio de sesión llamando al backend.
     */
    private void handleLogin(String email, String password) {
        HttpClient httpClient = HttpClient.newHttpClient();
        Gson gson = new Gson();

        try {
            String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String responseBody = resp.body();

            if (resp.statusCode() == 200) {
                // Leemos el JSON de respuesta
                JsonObject loginResponse = gson.fromJson(responseBody, JsonObject.class);
                String role = loginResponse.get("role").getAsString();
                String userId = loginResponse.get("id").getAsString(); // Obtenemos el ID

                dispose(); // Cerramos el login

                // Pasamos el ID al constructor del dashboard
                if (role.equals("admin")) {
                    new AdminDashboard(userId).setVisible(true); // Pasamos el ID
                } else if (role.equals("empleado")) {
                    // Asegúrate de que EmpleadoDashboard también acepte el ID
                    new EmpleadoDashboard(userId).setVisible(true); // Pasamos el ID
                }
            } else {
                // Leemos el mensaje de error del JSON si existe
                String errorMessage = "Correo o contraseña incorrectos.";
                try {
                    JsonObject errorResponse = gson.fromJson(responseBody, JsonObject.class);
                    if (errorResponse.has("message")) {
                        errorMessage = errorResponse.get("message").getAsString();
                    }
                } catch (Exception ignored) {} // Si no es JSON, usa el mensaje por defecto

                JOptionPane.showMessageDialog(this,
                        errorMessage,
                        "Error de autenticación", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de conexión con el servidor.",
                    "Conexión fallida", JOptionPane.ERROR_MESSAGE);
        }
    }

    public FoodFlowLogin() {
        super("FoodFlow – Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        /* ===== ROOT ===== */
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.FONDO);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));
        setContentPane(root);

        /* ===== HEADER ===== */
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel logo = new JLabel();
        logo.setHorizontalAlignment(SwingConstants.RIGHT);
        loadLogoRight(logo);
        header.add(logo, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        /* ===== CENTRO ===== */
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        root.add(center, BorderLayout.CENTER);
        final int COL_W = 520;
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setMaximumSize(new Dimension(COL_W, Integer.MAX_VALUE));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        center.add(col, gbc);

        /* ===== TÍTULO ===== */
        JLabel title = new JLabel("Inicia sesión", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE.deriveFont(28f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(8, 0, 20, 0));
        col.add(title);

        /* ===== CAMPOS ===== */
        PlaceholderField fEmail = new PlaceholderField("Correo electrónico");
        col.add(new LocalRoundedInput(fEmail, IconLabel.mail()));
        col.add(Box.createVerticalStrut(12));
        PlaceholderPassword fPass = new PlaceholderPassword("Contraseña");
        col.add(new LocalRoundedInput(fPass, IconLabel.lock()));
        col.add(Box.createVerticalStrut(10));

        /* ===== LINKS ===== */
        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        links.setOpaque(false);
        LinkLabel forgot = new LinkLabel("Olvidé la contraseña");
        LinkLabel help = new LinkLabel("Ayuda");
        links.add(forgot); links.add(help);
        links.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(links);
        col.add(Box.createVerticalStrut(20));

        /* ===== BOTÓN ===== */
        RoundedButton btn = new RoundedButton("Entrar");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(COL_W, 48));
        btn.setMaximumSize(new Dimension(COL_W, 48));
        getRootPane().setDefaultButton(btn);
        btn.addActionListener(e -> {
            String emailTxt = fEmail.getRealText();
            String passTxt = fPass.getRealText();
            if (emailTxt.isBlank() || passTxt.isBlank()) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa correo y contraseña.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            handleLogin(emailTxt, passTxt);
        });
        col.add(btn);
        col.add(Box.createVerticalStrut(16));

        /* ===== PIE ===== */
        JPanel reg = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        reg.setOpaque(false);
        JLabel noCuenta = new JLabel("¿Aún no tienes cuenta?");
        noCuenta.setFont(UIConstants.BASE); noCuenta.setForeground(UIConstants.TEXTO_SEC);
        LinkLabel registro = new LinkLabel("Registrarse");
        registro.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                new FoodFlowRegister().setVisible(true);
            }
        });
        reg.add(noCuenta); reg.add(registro);
        reg.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(reg);
    }

    /* ===== LOGO ===== */
    private void loadLogoRight(JLabel target) {
        try {
            URL img = getClass().getResource("/ima/Food1.png");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                Image scaled = icon.getImage().getScaledInstance(160, -1, Image.SCALE_SMOOTH);
                target.setIcon(new ImageIcon(scaled));
            } else { target.setText("FoodFlow"); target.setFont(UIConstants.LOGO); target.setForeground(UIConstants.VERDE_700); }
        } catch (Exception e) { target.setText("FoodFlow"); target.setFont(UIConstants.LOGO); target.setForeground(UIConstants.VERDE_700); }
    }

    /* ===== PLACEHOLDERS ===== */
    private static class PlaceholderField extends JTextField {
        private final String ph;
        PlaceholderField(String ph) { this.ph = ph; setBorder(null); setOpaque(false); setFont(UIConstants.BASE); setForeground(Color.BLACK); }
        String getRealText() { return getText().trim(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setColor(UIConstants.TEXTO_SEC); g2.setFont(getFont());
                g2.drawString(ph, 2, getBaseline(getWidth(), getHeight())); g2.dispose();
            }
        }
    }
    private static class PlaceholderPassword extends JPasswordField {
        private final String ph; private boolean hasText=false;
        PlaceholderPassword(String ph) {
            this.ph = ph; setBorder(null); setOpaque(false); setFont(UIConstants.BASE); setForeground(Color.BLACK); setEchoChar((char)0);
            getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                void upd(){ hasText = getPassword().length>0; setEchoChar(hasText ? '●' : (char)0); }
                public void insertUpdate(javax.swing.event.DocumentEvent e){ upd(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e){ upd(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e){ upd(); }
            });
        }
        String getRealText(){ return new String(getPassword()).trim(); }
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(!hasText && !isFocusOwner()){
                Graphics2D g2=(Graphics2D)g.create(); g2.setColor(UIConstants.TEXTO_SEC); g2.setFont(getFont());
                g2.drawString(ph,2,getBaseline(getWidth(),getHeight())); g2.dispose();
            }
        }
    }
    /* ===== LocalRoundedInput ===== */
    private static class LocalRoundedInput extends JPanel {
        private boolean focused = false;
        LocalRoundedInput(JComponent inner, JLabel icon) {
            super(new BorderLayout(12, 0)); setOpaque(false);
            int W = 520, H = 48;
            setPreferredSize(new Dimension(W, H)); setMaximumSize(new Dimension(W, H)); setMinimumSize(new Dimension(W, H));
            setBorder(new EmptyBorder(0, 0, 0, 0));
            JPanel box = new JPanel(new BorderLayout(12, 0)) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.setStroke(new BasicStroke(1.5f)); g2.setColor(focused ? UIConstants.VERDE_700 : UIConstants.BORDE_SUAVE);
                    g2.draw(new RoundRectangle2D.Double(0.75, 0.75, getWidth()-1.5, getHeight()-1.5, 12, 12));
                    g2.dispose();
                }
            };
            box.setOpaque(false); box.setBorder(new EmptyBorder(10, 14, 10, 14));
            if (icon != null) { icon.setForeground(UIConstants.TEXTO_SEC); box.add(icon, BorderLayout.WEST); }
            inner.setBorder(null); inner.setOpaque(false); inner.setFont(UIConstants.BASE); inner.setForeground(Color.BLACK);
            box.add(inner, BorderLayout.CENTER);
            inner.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
                @Override public void focusLost  (FocusEvent e) { focused = false; repaint(); }
            });
            add(box, BorderLayout.CENTER);
        }
    }

    // Main para probar solo esta ventana
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FoodFlowLogin().setVisible(true));
    }
}