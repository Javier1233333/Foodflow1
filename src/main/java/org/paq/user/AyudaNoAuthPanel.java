package org.paq.user;

import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AyudaNoAuthPanel extends JDialog {

    private final JFrame ownerFrame;
    private final HttpClient httpClient;
    private final Gson gson;

    private static final String SOPORTE_URL = "http://localhost:8081/api/public/soporte/reporte";

    // Componentes del formulario
    private JTextField fEmail;
    private JComboBox<String> fProblemaSelector;
    private JTextArea fMensajeDetalle; // Nuevo campo para "Otro"
    private JScrollPane scrollMensajeDetalle; // ScrollPane para el detalle

    private final String[] PROBLEMA_OPTIONS = {
            "Seleccionar un problema...",
            "Olvidé mi Contraseña",
            "Mi cuenta está bloqueada",
            "Error al iniciar sesión",
            "Problemas con la interfaz (UI)",
            "Otro (especificar abajo)"
    };

    public AyudaNoAuthPanel(JFrame ownerFrame) {
        super(ownerFrame, "Soporte Técnico - Sabora", true);

        this.ownerFrame = ownerFrame;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Contenedor principal con diseño y logo
        JPanel contentPane = createMainPanel();
        setContentPane(contentPane);

        initComponents(contentPane);

        pack();
        // El diálogo se centra sobre el Frame padre
        setLocationRelativeTo(ownerFrame);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(new Color(245, 245, 245)); // Fondo gris claro
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- LOGO EN CABECERA ---
        JLabel logoLabel = loadLogoLabel();
        logoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(logoLabel, BorderLayout.NORTH);

        return panel;
    }

    private void initComponents(JPanel contentPane) {

        // --- Panel Central del Formulario ---
        JPanel formWrapper = new JPanel(new BorderLayout(0, 15));
        formWrapper.setOpaque(false);

        // Título
        JLabel title = new JLabel("Solicitud de Contacto");
        title.setFont(UIConstants.TITLE.deriveFont(24f).deriveFont(Font.BOLD));
        formWrapper.add(title, BorderLayout.NORTH);

        // Grid para campos
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Email
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formGrid.add(new JLabel("Tu Correo Electrónico:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        fEmail = new JTextField(25);
        formGrid.add(fEmail, gbc);

        // Fila 1: Selector de Problema
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formGrid.add(new JLabel("Tipo de Problema:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        fProblemaSelector = new JComboBox<>(PROBLEMA_OPTIONS);
        fProblemaSelector.setSelectedIndex(0);
        formGrid.add(fProblemaSelector, gbc);

        // Fila 2: Área de Detalle (Inicialmente oculta)
        fMensajeDetalle = new JTextArea(4, 25);
        fMensajeDetalle.setLineWrap(true);
        fMensajeDetalle.setWrapStyleWord(true);
        scrollMensajeDetalle = new JScrollPane(fMensajeDetalle);
        scrollMensajeDetalle.setVisible(false); // Ocultar por defecto

        // Añadir el listener para mostrar/ocultar el campo de detalle
        fProblemaSelector.addActionListener(e -> toggleMensajeDetalle());

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; // Ocupa ambas columnas
        gbc.fill = GridBagConstraints.BOTH; // Permite que ocupe espacio vertical
        gbc.weighty = 1.0;
        formGrid.add(scrollMensajeDetalle, gbc);

        formWrapper.add(formGrid, BorderLayout.CENTER);
        contentPane.add(formWrapper, BorderLayout.CENTER);

        // --- Botones ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        RoundedButton btnCancelar = new RoundedButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        RoundedButton btnEnviar = new RoundedButton("Enviar Solicitud");
        btnEnviar.addActionListener(e -> enviarSolicitud());

        buttonsPanel.add(btnCancelar);
        buttonsPanel.add(btnEnviar);
        contentPane.add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void toggleMensajeDetalle() {
        boolean show = "Otro (especificar abajo)".equals(fProblemaSelector.getSelectedItem());
        scrollMensajeDetalle.setVisible(show);

        // Revalidar para que el layout manager recalcule el espacio
        SwingUtilities.getWindowAncestor(this).pack();
    }

    // --- Lógica de envío ---
    private void enviarSolicitud() {
        String email = fEmail.getText().trim();
        String tipoProblema = (String) fProblemaSelector.getSelectedItem();
        String detalle = fMensajeDetalle.getText().trim();

        if (email.isEmpty() || "Seleccionar un problema...".equals(tipoProblema)) {
            JOptionPane.showMessageDialog(this, "Completa el correo y selecciona un tipo de problema.", "Campos Requeridos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Si seleccionó "Otro" y no escribió el detalle
        if ("Otro (especificar abajo)".equals(tipoProblema) && detalle.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, especifica los detalles del problema.", "Detalle Requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Construir el mensaje final para el backend
        String mensajeFinal;
        if ("Otro (especificar abajo)".equals(tipoProblema)) {
            mensajeFinal = "TIPO: Otro. DETALLE: " + detalle;
        } else {
            mensajeFinal = "TIPO: " + tipoProblema;
        }

        // 2. Construir Payload
        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("mensaje", mensajeFinal);
        String jsonPayload = gson.toJson(payload);

        try {
            // ... (Resto de la lógica asíncrona de envío a SOPORTE_URL) ...
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(SOPORTE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        SwingUtilities.invokeLater(() -> {
                            if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                                JOptionPane.showMessageDialog(this,
                                        "Tu solicitud ha sido enviada. Te contactaremos pronto.",
                                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                                dispose(); // Cierra el diálogo al tener éxito
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "Error al enviar solicitud (HTTP " + resp.statusCode() + ").\nDetalles: " + resp.body(),
                                        "Error del Servidor", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this,
                                    "No se pudo conectar al servidor. Verifica la red.",
                                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                        });
                        return null;
                    });

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error interno al construir la petición.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Carga el logo de Sabora para la cabecera */
    private JLabel loadLogoLabel() {
        JLabel logoLabel = new JLabel();
        // Asumiendo que el logo de Sabora está disponible en recursos
        URL res = getClass().getResource("/ima/ggg.png");
        if (res != null) {
            ImageIcon icon = new ImageIcon(res);
            Image scaled = icon.getImage().getScaledInstance(120, -1, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaled));
        } else {
            logoLabel.setText("Sabora");
            logoLabel.setFont(UIConstants.LOGO.deriveFont(20f));
        }
        return logoLabel;
    }
}