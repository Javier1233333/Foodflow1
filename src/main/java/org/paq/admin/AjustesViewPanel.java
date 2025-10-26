package org.paq.admin;

import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class AjustesViewPanel extends JPanel {

    private final AdminViewContext context;

    public AjustesViewPanel(AdminViewContext context) {
        super(new BorderLayout());
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));
        initComponents();
    }

    private void initComponents() {
        JLabel title = new JLabel("Ajustes");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        this.add(title, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 30, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(40, 0, 0, 0));

        // Usa context.mainCards y context.mainPanel para la navegación
        cardsPanel.add(createSettingsCard("Ajustes usuarios", "user.png", "AJUSTES_USUARIOS"));
        cardsPanel.add(createSettingsCard("Vista rutas", "delivery-man.png", "AJUSTES_RUTAS"));
        cardsPanel.add(createSettingsCard("Inventarios", "clipboard.png", "INVENTARIO"));
        cardsPanel.add(createSettingsCard("Pagos y Nóminas", "cash.png", "NOMINAS"));

        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        this.add(cardsPanel, BorderLayout.CENTER);
    }

    private JPanel createSettingsCard(String text, String iconName, String cardKey) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBackground(UIConstants.VERDE_700);
        card.setBorder(new EmptyBorder(30, 20, 30, 20)); card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(UIConstants.VERDE_800); }
            @Override public void mouseExited(MouseEvent e) { card.setBackground(UIConstants.VERDE_700); }
            @Override public void mouseClicked(MouseEvent e) { context.mainCards.show(context.mainPanel, cardKey); } // Usa el contexto
        });
        URL res = getClass().getResource("/icons/" + iconName);
        if (res != null) {
            JLabel iconLabel = new JLabel(new ImageIcon(new ImageIcon(res).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(iconLabel);
        }
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(UIConstants.BASE.deriveFont(Font.BOLD, 16f));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(20));
        card.add(textLabel);
        // Limita el tamaño máximo
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }
}