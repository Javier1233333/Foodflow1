package org.paq;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Pantalla de Registro con lógica completa (validación + POST).
 */
public class FoodFlowRegister extends JFrame {

    // URL de la API (Ajustado al puerto 8081)
    private static final String API_REGISTER_URL = "http://localhost:8081/api/auth/register";

    // Paleta
    private static final Color COLOR_VERDE     = new Color(34, 139, 34);
    private static final Color COLOR_GRIS_TXT  = new Color(150, 150, 150);
    private static final Color COLOR_FONDO     = Color.WHITE;
    private static final Color COLOR_BORDE     = new Color(200, 200, 200);
    private static final Color COLOR_BORDE_FOC = new Color(34, 139, 34);

    // Fonts
    private static Font FONT_TITULO;
    private static Font FONT_NORMAL;
    private static Font FONT_LOGO;

    static {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            URL reg = FoodFlowRegister.class.getResource("/fonts/Inter-Regular.ttf");
            URL bold = FoodFlowRegister.class.getResource("/fonts/Inter-Bold.ttf");
            if (reg  != null) ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, reg.openStream()));
            if (bold != null) ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, bold.openStream()));
            FONT_TITULO = new Font("Inter", Font.BOLD, 28);
            FONT_NORMAL = new Font("Inter", Font.PLAIN, 14);
            FONT_LOGO   = new Font("Inter", Font.BOLD, 36);
        } catch (Exception e) {
            FONT_TITULO = new Font("SansSerif", Font.BOLD, 28);
            FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 14);
            FONT_LOGO   = new Font("SansSerif", Font.BOLD, 36);
        }
    }

    // HTTP Client
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Campos de la UI
    private PlaceholderField     fNombre;
    private PlaceholderField     fApellido;
    private PlaceholderField     fCodigoEmpresa;
    private PlaceholderField     fEmail;
    private PlaceholderPassword  fPass1;
    private PlaceholderPassword  fPass2;
    private JCheckBox            chkTerminos;
    private JButton              btnRegistro;

    public FoodFlowRegister() {
        super("Registrarse");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1110, 760);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_FONDO);
        header.setBorder(new EmptyBorder(20, 20, 0, 40));
        JLabel logo = new JLabel();
        logo.setHorizontalAlignment(SwingConstants.RIGHT);
        loadLogo(logo);
        header.add(logo, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Centro
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setBackground(COLOR_FONDO);
        add(centerWrap, BorderLayout.CENTER);

        JPanel col = new JPanel();
        col.setBackground(COLOR_FONDO);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Inicia con nosotros");
        title.setFont(FONT_TITULO);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(10, 0, 24, 0));
        col.add(title);

        // Inputs
        fNombre        = new PlaceholderField("Ingresa tu nombre");
        fApellido      = new PlaceholderField("Ingresa tu apellido");
        fCodigoEmpresa = new PlaceholderField("Agrega tu código de empresa");
        fEmail         = new PlaceholderField("Agrega tu correo electrónico");
        fPass1         = new PlaceholderPassword("Elige una contraseña");
        fPass2         = new PlaceholderPassword("Repite tu contraseña");

        col.add(new RoundedInput(fNombre,        IconLabel.user()));
        col.add(Box.createVerticalStrut(12));
        col.add(new RoundedInput(fApellido,      IconLabel.user()));
        col.add(Box.createVerticalStrut(12));
        col.add(new RoundedInput(fCodigoEmpresa, IconLabel.id()));
        col.add(Box.createVerticalStrut(12));
        col.add(new RoundedInput(fEmail,         IconLabel.mail()));
        col.add(Box.createVerticalStrut(12));
        col.add(new RoundedInput(fPass1,         IconLabel.lock()));
        col.add(Box.createVerticalStrut(12));
        col.add(new RoundedInput(fPass2,         IconLabel.lock()));
        col.add(Box.createVerticalStrut(16));

        // Checkbox
        chkTerminos = new JCheckBox("Estoy de acuerdo con los términos de uso");
        chkTerminos.setOpaque(false);
        chkTerminos.setFont(FONT_NORMAL);
        chkTerminos.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(chkTerminos);
        col.add(Box.createVerticalStrut(16));

        // Botón Registro
        btnRegistro = new RoundedButton("Registro");
        btnRegistro.setPreferredSize(new Dimension(520, 48));
        btnRegistro.setMaximumSize(new Dimension(520, 48));
        btnRegistro.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegistro.addActionListener(e -> tryRegister());
        col.add(btnRegistro);

        col.add(Box.createVerticalStrut(24));

        // Link a Login
        JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        loginRow.setBackground(COLOR_FONDO);
        JLabel lbl = new JLabel("Ya tengo cuenta");
        lbl.setFont(FONT_NORMAL);
        lbl.setForeground(COLOR_GRIS_TXT);
        LinkLabel toLogin = new LinkLabel("Iniciar sesión");
        toLogin.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                new FoodFlowLogin().setVisible(true);
            }
        });
        loginRow.add(lbl);
        loginRow.add(toLogin);
        loginRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(loginRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        centerWrap.add(col, gbc);
    }

    /* ===================== LÓGICA DE REGISTRO ===================== */

    private void tryRegister() {
        String nombre   = fNombre.getRealText();
        String apellido = fApellido.getRealText();
        String codigo   = fCodigoEmpresa.getRealText();
        String email    = fEmail.getRealText();
        String pass1    = fPass1.getRealText();
        String pass2    = fPass2.getRealText();

        // Validaciones locales
        StringBuilder err = new StringBuilder();
        if (nombre.isBlank())   err.append("• Ingresa tu nombre.\n");
        if (apellido.isBlank()) err.append("• Ingresa tu apellido.\n");
        if (codigo.isBlank())   err.append("• Ingresa tu código de empresa.\n");
        if (email.isBlank())    err.append("• Ingresa tu correo electrónico.\n");
        if (!isValidEmail(email)) err.append("• El correo no tiene un formato válido.\n");
        if (pass1.isBlank())    err.append("• Ingresa una contraseña.\n");
        if (pass1.length() < 6) err.append("• La contraseña debe tener al menos 6 caracteres.\n");
        if (!pass1.equals(pass2)) err.append("• Las contraseñas no coinciden.\n");
        if (!chkTerminos.isSelected()) err.append("• Debes aceptar los términos de uso.\n");

        if (err.length() > 0) {
            JOptionPane.showMessageDialog(this, err.toString(), "Verifica los datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Deshabilita UI y envía
        setFormEnabled(false);
        new SwingWorker<Void, Void>() {
            int status = -1;
            String bodyResp = null;
            @Override protected Void doInBackground() {
                try {
                    // JSON ajustado para incluir todos los campos requeridos por el backend
                    String json = String.format(
                            "{\"name\":\"%s\",\"lastName\":\"%s\",\"companyCode\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                            escapeJson(nombre), escapeJson(apellido), escapeJson(codigo),
                            escapeJson(email), escapeJson(pass1)
                    );

                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(API_REGISTER_URL)) // Usando 8081
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .timeout(Duration.ofSeconds(15))
                            .build();

                    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                    status = resp.statusCode();
                    bodyResp = resp.body();
                } catch (IOException | InterruptedException ex) {
                    bodyResp = "No se pudo conectar con el servidor (Verifique el puerto 8081).\n" + ex.getMessage();
                    status = -1;
                }
                return null;
            }

            @Override protected void done() {
                setFormEnabled(true);
                if (status == 200 || status == 201) {
                    JOptionPane.showMessageDialog(FoodFlowRegister.this,
                            "¡Cuenta creada con éxito! Ahora puedes iniciar sesión.",
                            "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    new FoodFlowLogin().setVisible(true);
                } else if (status == 400) {
                    String msg = "Error: El correo electrónico ya está registrado.";
                    if (bodyResp != null && !bodyResp.isBlank()) msg += "\nDetalle: " + bodyResp;
                    JOptionPane.showMessageDialog(FoodFlowRegister.this, msg,
                            "Fallo de Registro", JOptionPane.ERROR_MESSAGE);
                } else if (status == -1) {
                    JOptionPane.showMessageDialog(FoodFlowRegister.this, bodyResp,
                            "Error de conexión", JOptionPane.ERROR_MESSAGE);
                } else {
                    String msg = "Error inesperado del servidor: " + status;
                    if (bodyResp != null && !bodyResp.isBlank()) msg += "\nDetalle: " + bodyResp;
                    JOptionPane.showMessageDialog(FoodFlowRegister.this, msg,
                            "Error en el registro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setFormEnabled(boolean enabled) {
        fNombre.setEnabled(enabled);
        fApellido.setEnabled(enabled);
        fCodigoEmpresa.setEnabled(enabled);
        fEmail.setEnabled(enabled);
        fPass1.setEnabled(enabled);
        fPass2.setEnabled(enabled);
        chkTerminos.setEnabled(enabled);
        btnRegistro.setEnabled(enabled);
        btnRegistro.setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.WAIT_CURSOR));
    }

    private static boolean isValidEmail(String s) {
        String r = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$";
        return Pattern.compile(r, Pattern.CASE_INSENSITIVE).matcher(s).matches();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /* ===================== UI HELPERS Y CLASES INTERNAS ===================== */

    private void loadLogo(JLabel target) {
        try {
            URL img = FoodFlowRegister.class.getResource("/ima/Food1.png");
            if (img == null) img = FoodFlowRegister.class.getResource("/ima/Food1.png");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                int w = (int)(icon.getIconWidth() * 0.8);
                int h = (int)(icon.getIconHeight() * 0.8);
                Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                target.setIcon(new ImageIcon(scaled));
            } else {
                target.setText("FoodFlow");
                target.setFont(FONT_LOGO);
                target.setForeground(COLOR_VERDE);
            }
        } catch (Exception e) {
            target.setText("FoodFlow");
            target.setFont(FONT_LOGO);
            target.setForeground(COLOR_VERDE);
        }
    }

    private class RoundedInput extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JComponent inner;
        private final JLabel icon;
        private boolean focused = false;

        RoundedInput(JComponent inner, JLabel icon) {
            super(new BorderLayout(12, 0));
            this.inner = inner;
            this.icon  = icon;
            setOpaque(false);
            setPreferredSize(new Dimension(520, 48));
            setMaximumSize(new Dimension(520, 48));
            setBorder(new EmptyBorder(0, 0, 0, 0));

            JPanel box = new JPanel(new BorderLayout(12, 0)) {
                private static final long serialVersionUID = 1L;
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.setColor(focused ? COLOR_BORDE_FOC : COLOR_BORDE);
                    g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth()-1, getHeight()-1, 12, 12));
                    g2.dispose();
                }
            };
            box.setOpaque(false);
            box.setBorder(new EmptyBorder(8, 14, 8, 14));
            icon.setForeground(COLOR_GRIS_TXT);
            box.add(icon, BorderLayout.WEST);
            box.add(inner, BorderLayout.CENTER);
            add(box, BorderLayout.CENTER);

            inner.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { focused = true;  repaint(); }
                @Override public void focusLost  (FocusEvent e)   { focused = false; repaint(); }
            });
        }
    }

    private class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        RoundedButton(String text) {
            super(text);
            setFont(FONT_NORMAL.deriveFont(Font.BOLD));
            setForeground(Color.WHITE);
            setBackground(COLOR_VERDE);
            setFocusPainted(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { setBackground(COLOR_VERDE.darker()); }
                @Override public void mouseExited (MouseEvent e) { setBackground(COLOR_VERDE); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics(getFont());
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setFont(getFont());
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
        @Override public void setContentAreaFilled(boolean b) {}
        @Override public void setBorderPainted(boolean b) {}
    }

    public static class LinkLabel extends JLabel {
        private static final long serialVersionUID = 1L;
        LinkLabel(String text) {
            super(text);
            setFont(FONT_NORMAL.deriveFont(Font.BOLD));
            setForeground(COLOR_VERDE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { setText("<html><u>" + text + "</u></html>"); }
                @Override public void mouseExited (MouseEvent e) { setText(text); }
            });
        }
    }

    private static class IconLabel {
        static JLabel user() {
            JLabel l = new JLabel("\uD83D\uDC64");
            l.setFont(FONT_NORMAL.deriveFont(18f));
            l.setForeground(COLOR_GRIS_TXT);
            return l;
        }
        static JLabel id() {
            JLabel l = new JLabel("\uD83C\uDD94");
            l.setFont(FONT_NORMAL.deriveFont(18f));
            l.setForeground(COLOR_GRIS_TXT);
            return l;
        }
        static JLabel mail() {
            JLabel l = new JLabel("\uD83D\uDCE7");
            l.setFont(FONT_NORMAL.deriveFont(18f));
            l.setForeground(COLOR_GRIS_TXT);
            return l;
        }
        static JLabel lock() {
            JLabel l = new JLabel("\uD83D\uDD12");
            l.setFont(FONT_NORMAL.deriveFont(18f));
            l.setForeground(COLOR_GRIS_TXT);
            return l;
        }
    }

    private static class PlaceholderField extends JTextField {
        private final String placeholder;
        PlaceholderField(String ph) {
            super();
            this.placeholder = ph;
            setBorder(null);
            setOpaque(false);
            setFont(FONT_NORMAL);
            setForeground(Color.BLACK);
        }
        String getRealText() { return super.getText().trim(); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(COLOR_GRIS_TXT);
                g2.setFont(getFont());
                g2.drawString(placeholder, 2, getBaseline(getWidth(), getHeight()));
                g2.dispose();
            }
        }
    }

    private static class PlaceholderPassword extends JPasswordField {
        private final String placeholder;
        private boolean hasText = false;
        PlaceholderPassword(String ph) {
            this.placeholder = ph;
            setBorder(null);
            setOpaque(false);
            setFont(FONT_NORMAL);
            setForeground(Color.BLACK);
            setEchoChar((char)0);
            getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                void upd() {
                    hasText = getPassword().length > 0;
                    setEchoChar(hasText ? '●' : (char)0);
                }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { upd(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { upd(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { upd(); }
            });
        }
        String getRealText() { return new String(getPassword()).trim(); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!hasText && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(COLOR_GRIS_TXT);
                g2.setFont(getFont());
                g2.drawString(placeholder, 2, getBaseline(getWidth(), getHeight()));
                g2.dispose();
            }
        }
    }

    /* ===================== MAIN ===================== */
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FoodFlowRegister().setVisible(true));
    }
}