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

public class FoodFlowRegister extends JFrame {

    /**
     * Lógica para manejar el registro de un nuevo usuario.
     */
    private void handleRegister(String nombre, String apellidos, String email, String password) {
        HttpClient httpClient = HttpClient.newHttpClient();
        Gson gson = new Gson();

        try {
            // 1. Creamos el JSON con todos los datos
            String body = String.format(
                    "{\"nombre\":\"%s\", \"apellidos\":\"%s\", \"email\":\"%s\", \"password\":\"%s\"}",
                    nombre, apellidos, email, password
            );

            // 2. Apuntamos al endpoint de registro
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 201) { // 201 = Creado (Éxito)
                JOptionPane.showMessageDialog(this,
                        "¡Registro exitoso! Revisa la consola del servidor para tu código.",
                        "Registro Completo", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Cerramos registro
                new FoodFlowLogin().setVisible(true); // Abrimos login
            } else if (resp.statusCode() == 409) { // 409 = Conflicto (Email ya existe)
                JOptionPane.showMessageDialog(this,
                        "El correo electrónico ya está registrado.",
                        "Error de Registro", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error del servidor: " + resp.statusCode() + "\n" + resp.body(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de conexión con el servidor.",
                    "Conexión fallida", JOptionPane.ERROR_MESSAGE);
        }
    }

    public FoodFlowRegister() {
        super("FoodFlow – Registrarse");
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
        JLabel title = new JLabel("Crea tu cuenta", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE.deriveFont(28f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(8, 0, 20, 0));
        col.add(title);

        /* ===== CAMPOS ===== */
        PlaceholderField fNombre = new PlaceholderField("Nombre(s)");
        col.add(new LocalRoundedInput(fNombre, IconLabel.user()));
        col.add(Box.createVerticalStrut(12));
        PlaceholderField fApellidos = new PlaceholderField("Apellidos");
        col.add(new LocalRoundedInput(fApellidos, IconLabel.user()));
        col.add(Box.createVerticalStrut(12));
        PlaceholderField fEmail = new PlaceholderField("Correo electrónico");
        col.add(new LocalRoundedInput(fEmail, IconLabel.mail()));
        col.add(Box.createVerticalStrut(12));
        PlaceholderPassword fPass = new PlaceholderPassword("Contraseña");
        col.add(new LocalRoundedInput(fPass, IconLabel.lock()));
        col.add(Box.createVerticalStrut(20));

        /* ===== BOTÓN ===== */
        RoundedButton btn = new RoundedButton("Crear cuenta");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(COL_W, 48));
        btn.setMaximumSize(new Dimension(COL_W, 48));
        getRootPane().setDefaultButton(btn);
        btn.addActionListener(e -> {
            String nombreTxt = fNombre.getRealText();
            String apellidosTxt = fApellidos.getRealText();
            String emailTxt = fEmail.getRealText();
            String passTxt = fPass.getRealText();
            if (nombreTxt.isBlank() || apellidosTxt.isBlank() || emailTxt.isBlank() || passTxt.isBlank()) {
                JOptionPane.showMessageDialog(this, "Por favor, completa todos los campos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            handleRegister(nombreTxt, apellidosTxt, emailTxt, passTxt);
        });
        col.add(btn);
        col.add(Box.createVerticalStrut(16));

        /* ===== PIE ===== */
        JPanel reg = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        reg.setOpaque(false);
        JLabel yaCuenta = new JLabel("¿Ya tienes cuenta?");
        yaCuenta.setFont(UIConstants.BASE); yaCuenta.setForeground(UIConstants.TEXTO_SEC);
        LinkLabel loginLink = new LinkLabel("Iniciar sesión");
        loginLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                new FoodFlowLogin().setVisible(true);
            }
        });
        reg.add(yaCuenta); reg.add(loginLink);
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
        SwingUtilities.invokeLater(() -> new FoodFlowRegister().setVisible(true));
    }
}