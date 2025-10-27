package org.paq.admin;

import org.paq.AdminDashboard;
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
// --- Imports añadidos para la petición HTTP ---
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
// ---------------------------------------------

public class AyudaViewPanel extends JPanel {

    private final AdminViewContext context;
    private final AdminDashboard parentFrame; // Para mostrar el diálogo
    private final JTextArea messageArea;

    // Cliente HTTP para reutilizarlo
    private final HttpClient httpClient;

    public AyudaViewPanel(AdminViewContext context, AdminDashboard parentFrame) {
        super(new BorderLayout(0, 20));
        this.context = context;
        this.parentFrame = parentFrame;
        this.httpClient = HttpClient.newHttpClient(); // Inicializa el cliente

        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JPanel titlePanel = new JPanel(); titlePanel.setOpaque(false); titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Ayuda"); title.setFont(UIConstants.TITLE.deriveFont(32f)); title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Describe tu problema:"); subtitle.setFont(UIConstants.BASE.deriveFont(18f)); subtitle.setForeground(Color.GRAY); subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(title); titlePanel.add(Box.createVerticalStrut(10)); titlePanel.add(subtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        this.add(headerPanel, BorderLayout.NORTH);

        // --- Área de Texto ---
        messageArea = new JTextArea();
        messageArea.setFont(UIConstants.BASE.deriveFont(16f));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1));
        this.add(scrollPane, BorderLayout.CENTER);

        // --- Botones ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        RoundedButton backButton = new RoundedButton("Regresar al Menú");
        backButton.addActionListener(e -> context.mainCards.show(context.mainPanel, "MENU"));

        RoundedButton sendButton = new RoundedButton("Enviar");

        // --- INICIO DE LA LÓGICA DE ENVÍO ---
        sendButton.addActionListener(e -> {
            String mensajeProblema = messageArea.getText();

            // 1. Validar que el mensaje no esté vacío
            if (mensajeProblema.trim().isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame,
                        "Por favor, describe tu problema antes de enviar.",
                        "Mensaje Vacío",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. TODO: Obtener el ID del admin (¡Debes reemplazar esto!)
            // Asumo que tu 'context' tiene alguna forma de darte el admin logueado.
            String adminId = context.userId;// Ej: context.getSesion().getAdminId();

            // 3. TODO: Definir la URL de tu API de backend
            // Esta es la URL de tu servidor Spring Boot donde creaste el Controller
            String tuApiDeSoporteUrl = "http://localhost:8081/api/admin/soporte/reporte";
            // 4. Crear el JSON para enviar
            String jsonPayload = String.format(
                    "{\"adminId\": \"%s\", \"mensaje\": \"%s\"}",
                    adminId,
                    // Escapamos caracteres especiales para que sea un JSON válido
                    mensajeProblema.replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t")
            );

            try {
                // 5. Crear la petición HTTP
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(tuApiDeSoporteUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                // 6. Enviar de forma asíncrona (para no congelar la app)
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            // Se ejecuta en otro hilo cuando el servidor responde
                            System.out.println("Respuesta del Backend: " + response.statusCode());
                        })
                        .exceptionally(ex -> {
                            // Se ejecuta si la petición falla (ej. servidor caído)
                            System.err.println("Error al contactar el API de soporte: " + ex.getMessage());

                            // IMPORTANTE: Mostrar UI de error en el hilo de Swing
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(parentFrame,
                                        "No se pudo conectar al servidor de soporte.\nInténtalo más tarde.",
                                        "Error de Red",
                                        JOptionPane.ERROR_MESSAGE);
                            });
                            return null;
                        });

                // 7. Mostrar agradecimiento y limpiar la UI (INMEDIATAMENTE)
                // No esperamos a que el servidor responda, damos feedback al instante.
                parentFrame.showThankYouDialog();
                messageArea.setText("");
                context.mainCards.show(context.mainPanel, "MENU");

            } catch (Exception ex) {
                // Error al crear la URI (poco probable)
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parentFrame,
                        "Ocurrió un error interno al construir la petición.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        // --- FIN DE LA LÓGICA DE ENVÍO ---

        buttonsPanel.add(backButton);
        buttonsPanel.add(sendButton);
        this.add(buttonsPanel, BorderLayout.SOUTH);
    }
}