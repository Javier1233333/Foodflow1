package paq;

import javax.swing.*;
import javax.swing.border.*;

import paq.FoodFlowRegister.LinkLabel;

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

/**
 * Pantalla de Login estilo Figma para FoodFlow.
 */
public class FoodFlowLogin extends JFrame {

    // Paleta
    private static final Color COLOR_VERDE     = new Color(34, 139, 34);
    private static final Color COLOR_GRIS_TXT  = new Color(150, 150, 150);
    private static final Color COLOR_FONDO     = Color.WHITE;
    private static final Color COLOR_BORDE     = new Color(200, 200, 200);
    private static final Color COLOR_BORDE_FOC = new Color(34, 139, 34);

    // Tipos (Inter con fallbacks)
    private static Font FONT_TITULO;
    private static Font FONT_NORMAL;
    private static Font FONT_LOGO;

    static {
        // Intenta registrar Inter desde recursos; si no, usa SansSerif
        try {
            // Coloca estos ttf en resources/fonts/
            // Inter-Regular.ttf  e  Inter-Bold.ttf
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            URL reg = FoodFlowLogin.class.getResource("/fonts/Inter-Regular.ttf");
            URL bold = FoodFlowLogin.class.getResource("/fonts/Inter-Bold.ttf");
            if (reg != null) ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, reg.openStream()));
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

    // HTTP client
    private final HttpClient httpClient;

    public FoodFlowLogin() {
        super("Inicio de sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1110, 760); // tamaño cercano al canvas de tu captura
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_FONDO);

        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        setLayout(new BorderLayout());

        // Header con logo (arriba derecha)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_FONDO);
        header.setBorder(new EmptyBorder(20, 20, 0, 40));
        JLabel logo = new JLabel();
        logo.setHorizontalAlignment(SwingConstants.RIGHT);
        loadLogo(logo); // intenta cargar Food1.png
        header.add(logo, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Columna central
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setBackground(COLOR_FONDO);
        add(centerWrap, BorderLayout.CENTER);

        JPanel col = new JPanel();
        col.setBackground(COLOR_FONDO);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Inicia sesión");
        title.setFont(FONT_TITULO);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(10, 0, 30, 0));
        col.add(title);

        // Campos
        PlaceholderField email = new PlaceholderField("Correo electrónico");
        RoundedInput emailInput = new RoundedInput(email, IconLabel.mail());

        PlaceholderPassword password = new PlaceholderPassword("Contraseña");
        RoundedInput passInput = new RoundedInput(password, IconLabel.lock());

        col.add(emailInput);
        col.add(Box.createVerticalStrut(12));
        col.add(passInput);
        col.add(Box.createVerticalStrut(8));

        // Links
        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        links.setBackground(COLOR_FONDO);
        LinkLabel forgot = new LinkLabel("Olvidé la contraseña");
        LinkLabel help   = new LinkLabel("Ayuda");
        links.add(forgot);
        links.add(help);
        links.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(links);
        col.add(Box.createVerticalStrut(24));

        // Botón Entrar
        JButton btn = new RoundedButton("Entrar");
        btn.setPreferredSize(new Dimension(520, 48));
        btn.setMaximumSize(new Dimension(520, 48));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            String emailTxt = email.getRealText();
            String passTxt  = password.getRealText();
            if (emailTxt.isBlank() || passTxt.isBlank()) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa tu correo y contraseña.",
                        "Campos vacíos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            handleLogin(emailTxt, passTxt);
        });
        col.add(btn);

        col.add(Box.createVerticalStrut(28));

        // Registrarse
        JPanel reg = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        reg.setBackground(COLOR_FONDO);
        JLabel noCuenta = new JLabel("Aún no tengo cuenta");
        noCuenta.setFont(FONT_NORMAL);
        noCuenta.setForeground(COLOR_GRIS_TXT);
        LinkLabel registro = new LinkLabel("Registrarse");
        registro.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                // Abre tu frame de login. Usa tu clase real si el nombre difiere.
                new FoodFlowRegister().setVisible(true);
            }
        });
        reg.add(noCuenta);
        reg.add(registro);
        reg.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(reg);

        // Centrado
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        centerWrap.add(col, gbc);
    }

    // === HTTP ===
    private void handleLogin(String email, String password) {
        try {
            String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/login")) // <-- tu backend
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JOptionPane.showMessageDialog(this, "¡Inicio de sesión exitoso!", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                String msg = "Error: " + resp.statusCode();
                if (resp.body() != null && !resp.body().isBlank()) msg += "\n" + resp.body();
                JOptionPane.showMessageDialog(this, msg, "Error de autenticación",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar con el servidor.\n" + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    // === Utilidades UI ===
    private void loadLogo(JLabel target) {
        try {
            URL img = FoodFlowLogin.class.getResource("Food1.png");
            if (img == null) img = FoodFlowLogin.class.getResource("/paq/Food1.png");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                // Escala suave (ajusta a tu tamaño deseado)
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

    /** Panel de input con borde redondo e icono a la izquierda */
    private class RoundedInput extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JComponent inner;
        private final JLabel icon;
        private boolean focused = false;

        RoundedInput(JComponent inner, JLabel icon) {
            super(new BorderLayout(12, 0));
            this.inner = inner;
            this.icon = icon;
            setBackground(COLOR_FONDO);
            setOpaque(false);

            setPreferredSize(new Dimension(520, 48));
            setMaximumSize(new Dimension(520, 48));
            setBorder(new EmptyBorder(0, 0, 0, 0));

            JPanel box = new JPanel(new BorderLayout(12, 0)) {
                /**
				 * 
				 */
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
                @Override public void focusLost(FocusEvent e)   { focused = false; repaint(); }
            });
        }
    }

    /** Botón redondeado estilo Figma */
    private class RoundedButton extends JButton {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		RoundedButton(String text) {
            super(text);
            setFont(FONT_NORMAL.deriveFont(Font.BOLD));
            setForeground(Color.WHITE);
            setBackground(COLOR_VERDE);
            setFocusPainted(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Hover effect
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    setBackground(COLOR_VERDE.darker());
                }
                @Override public void mouseExited(MouseEvent e) {
                    setBackground(COLOR_VERDE);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
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

        @Override public void setContentAreaFilled(boolean b) { /* noop */ }
        @Override public void setBorderPainted(boolean b) { /* noop */ }
    }

    /** Label con estilo link */
    private static class LinkLabel extends JLabel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		LinkLabel(String text) {
            super(text);
            setFont(FONT_NORMAL.deriveFont(Font.BOLD));
            setForeground(COLOR_VERDE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    setText("<html><u>" + text + "</u></html>");
                }
                @Override public void mouseExited(MouseEvent e) {
                    setText(text);
                }
            });
        }
    }

    /** Iconos simples (puedes reemplazar por SVG/PNG si lo prefieres) */
    private static class IconLabel {
        static JLabel mail() {
            JLabel l = new JLabel("\uD83D\uDCE7"); // sobre
            l.setFont(FONT_NORMAL.deriveFont(18f));
            l.setForeground(COLOR_GRIS_TXT);
            return l;
        }
        static JLabel lock() {
            JLabel l = new JLabel("\uD83D\uDD12"); // candado
            l.setFont(FONT_NORMAL.deriveFont(18f));
            l.setForeground(COLOR_GRIS_TXT);
            return l;
        }
    }

    /** TextField con placeholder real */
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

    /** PasswordField con placeholder sin bullets hasta escribir */
    private static class PlaceholderPassword extends JPasswordField {
        private final String placeholder;
        private boolean hasText = false;
        PlaceholderPassword(String ph) {
            this.placeholder = ph;
            setBorder(null);
            setOpaque(false);
            setFont(FONT_NORMAL);
            setForeground(Color.BLACK);
            setEchoChar((char)0); // muestra placeholder
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

    // Main
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FoodFlowLogin().setVisible(true));
    }
}

