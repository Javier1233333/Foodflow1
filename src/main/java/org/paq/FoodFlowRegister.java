package org.paq;

import org.paq.UI.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Pattern;

public class FoodFlowRegister extends JFrame {

    private final HttpClient http;

    public FoodFlowRegister() {
        super("FoodFlow – Registro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 820);
        setLocationRelativeTo(null);
        setResizable(false);

        http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Root
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.FONDO);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));
        setContentPane(root);

        // Logo (arriba derecha como tu captura) – si prefieres centrado, usa BorderLayout.NORTH con CENTER
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel logo = new JLabel();
        logo.setHorizontalAlignment(SwingConstants.RIGHT);
        loadLogoRight(logo);
        header.add(logo, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Centro
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        center.setOpaque(false);
        root.add(center, BorderLayout.CENTER);

        final int COL_W = 540;
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        //col.setPreferredSize(new Dimension(COL_W, 10));
        col.setMaximumSize(new Dimension(COL_W, Integer.MAX_VALUE));
        center.add(col);

        JLabel title = new JLabel("Inicia con nosotros", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE.deriveFont(20f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(8, 0, 16, 0));
        col.add(title);

        // Campos
        PlaceholderField fNombre        = new PlaceholderField("Nombre");
        PlaceholderField fApellido      = new PlaceholderField("Ingresa tu apellido");
        PlaceholderField fCodigoEmpresa = new PlaceholderField("Agrega tu código de empresa");
        PlaceholderField fEmail         = new PlaceholderField("Agrega tu correo electrónico");
        PlaceholderPassword fPass1      = new PlaceholderPassword("Elige una contraseña");
        PlaceholderPassword fPass2      = new PlaceholderPassword("Repite tu contraseña");

        col.add(new RoundedInput(fNombre,        IconLabel.user()));
        col.add(Box.createVerticalStrut(10));
        col.add(new RoundedInput(fApellido,      IconLabel.user()));
        col.add(Box.createVerticalStrut(10));
        col.add(new RoundedInput(fCodigoEmpresa, IconLabel.id()));
        col.add(Box.createVerticalStrut(10));
        col.add(new RoundedInput(fEmail,         IconLabel.mail()));
        col.add(Box.createVerticalStrut(10));
        col.add(new RoundedInput(fPass1,         IconLabel.lock()));
        col.add(Box.createVerticalStrut(10));
        col.add(new RoundedInput(fPass2,         IconLabel.lock()));
        col.add(Box.createVerticalStrut(12));

        JCheckBox chkTerminos = new JCheckBox("Estoy de acuerdo con los términos de uso");
        chkTerminos.setOpaque(false);
        chkTerminos.setFont(UIConstants.BASE);
        chkTerminos.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(chkTerminos);
        col.add(Box.createVerticalStrut(12));

        RoundedButton btnRegistro = new RoundedButton("Registro");
        btnRegistro.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegistro.setPreferredSize(new Dimension(COL_W, 48));
        btnRegistro.setMaximumSize(new Dimension(COL_W, 48));
        col.add(btnRegistro);

        col.add(Box.createVerticalStrut(16));

        JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        loginRow.setOpaque(false);
        JLabel ya = new JLabel("Ya tengo cuenta");
        ya.setFont(UIConstants.BASE);
        ya.setForeground(UIConstants.TEXTO_SEC);
        LinkLabel toLogin = new LinkLabel("Iniciar sesión");
        toLogin.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                new FoodFlowLogin().setVisible(true);
            }
        });
        loginRow.add(ya);
        loginRow.add(toLogin);
        loginRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(loginRow);

        // Acción de registro
        btnRegistro.addActionListener(e -> tryRegister(
                fNombre.getRealText(),
                fApellido.getRealText(),
                fCodigoEmpresa.getRealText(),
                fEmail.getRealText(),
                fPass1.getRealText(),
                fPass2.getRealText(),
                chkTerminos.isSelected()
        ));
        getRootPane().setDefaultButton(btnRegistro);
    }

    private void tryRegister(String nombre, String apellido, String codigo, String email,
                             String pass1, String pass2, boolean terminos) {

        StringBuilder err = new StringBuilder();
        if (nombre.isBlank())            err.append("• Ingresa tu nombre.\n");
        if (apellido.isBlank())          err.append("• Ingresa tu apellido.\n");
        if (codigo.isBlank())            err.append("• Ingresa tu código de empresa.\n");
        if (email.isBlank())             err.append("• Ingresa tu correo electrónico.\n");
        if (!isValidEmail(email))        err.append("• El correo no tiene un formato válido.\n");
        if (pass1.isBlank())             err.append("• Ingresa una contraseña.\n");
        if (pass1.length() < 6)          err.append("• La contraseña debe tener al menos 6 caracteres.\n");
        if (!pass1.equals(pass2))        err.append("• Las contraseñas no coinciden.\n");
        if (!terminos)                   err.append("• Debes aceptar los términos de uso.\n");

        if (err.length() > 0) {
            JOptionPane.showMessageDialog(this, err.toString(), "Verifica los datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Void, Void> sw = new SwingWorker<>() {
            int status = -1;
            String body = null;

            @Override protected Void doInBackground() {
                try {
                    String json = String.format(
                            "{\"name\":\"%s\",\"lastName\":\"%s\",\"companyCode\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                            esc(nombre), esc(apellido), esc(codigo), esc(email), esc(pass1)
                    );

                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/auth/register")) // ajusta si cambia
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofSeconds(15))
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                    status = resp.statusCode();
                    body = resp.body();
                } catch (Exception ex) {
                    status = -1;
                    body = "No se pudo conectar con el servidor.\n" + ex.getMessage();
                }
                return null;
            }

            @Override protected void done() {
                if (status == 200 || status == 201) {
                    JOptionPane.showMessageDialog(FoodFlowRegister.this,
                            "¡Cuenta creada con éxito! Ahora puedes iniciar sesión.",
                            "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    new FoodFlowLogin().setVisible(true);
                } else if (status == -1) {
                    JOptionPane.showMessageDialog(FoodFlowRegister.this, body,
                            "Error de conexión", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(FoodFlowRegister.this,
                            "Error " + status + (body != null ? "\n" + body : ""),
                            "Error en el registro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        sw.execute();
    }

    private static boolean isValidEmail(String s) {
        String r = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$";
        return java.util.regex.Pattern.compile(r, Pattern.CASE_INSENSITIVE).matcher(s).matches();
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void loadLogoRight(JLabel target) {
        try {
            URL img = FoodFlowRegister.class.getResource("/ima/Food1.png");
            if (img == null) img = FoodFlowRegister.class.getResource("Food1.png");
            if (img == null) img = FoodFlowRegister.class.getResource("/paq/Food1.png");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                Image scaled = icon.getImage().getScaledInstance(160, -1, Image.SCALE_SMOOTH);
                target.setIcon(new ImageIcon(scaled));
            } else {
                target.setText("FoodFlow");
                target.setFont(UIConstants.LOGO);
                target.setForeground(UIConstants.VERDE_700);
            }
        } catch (Exception e) {
            target.setText("FoodFlow");
            target.setFont(UIConstants.LOGO);
            target.setForeground(UIConstants.VERDE_700);
        }
    }

    /* ====== Inputs con placeholder ====== */
    private static class PlaceholderField extends JTextField {
        private final String ph;
        PlaceholderField(String ph) { this.ph = ph;
            setBorder(null); setOpaque(false); setFont(UIConstants.BASE); setForeground(Color.BLACK); }
        String getRealText() { return getText().trim(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(UIConstants.TEXTO_SEC); g2.setFont(getFont());
                g2.drawString(ph, 2, getBaseline(getWidth(), getHeight())); g2.dispose();
            }
        }
    }

    private static class PlaceholderPassword extends JPasswordField {
        private final String ph; private boolean hasText=false;
        PlaceholderPassword(String ph) { this.ph = ph;
            setBorder(null); setOpaque(false); setFont(UIConstants.BASE); setForeground(Color.BLACK);
            setEchoChar((char)0);
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
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(UIConstants.TEXTO_SEC); g2.setFont(getFont());
                g2.drawString(ph,2,getBaseline(getWidth(),getHeight())); g2.dispose();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FoodFlowRegister().setVisible(true));
    }
}
