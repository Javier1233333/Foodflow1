package org.paq.views.admin;

import org.paq.AdminDashboard; // Necesita el JFrame principal
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AyudaViewPanel extends JPanel {

    private final AdminViewContext context;
    private final AdminDashboard parentFrame; // Para mostrar el diálogo
    private final JTextArea messageArea;

    public AyudaViewPanel(AdminViewContext context, AdminDashboard parentFrame) {
        super(new BorderLayout(0, 20));
        this.context = context;
        this.parentFrame = parentFrame;

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
        sendButton.addActionListener(e -> {
            // TODO: Enviar el mensaje a algún lugar (email, base de datos de soporte, etc.)
            parentFrame.showThankYouDialog(); // Llama al método del JFrame principal
            messageArea.setText("");
            context.mainCards.show(context.mainPanel, "MENU");
        });
        buttonsPanel.add(backButton);
        buttonsPanel.add(sendButton);
        this.add(buttonsPanel, BorderLayout.SOUTH);
    }
}