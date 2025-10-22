package org.paq.views.admin;

import com.google.gson.JsonObject;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainMenuView extends JPanel {

    private final AdminViewContext context;

    public MainMenuView(AdminViewContext context) {
        super(new BorderLayout());
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));
        initComponents();
    }

    private void initComponents() {
        JLabel title = new JLabel("Menú Principal");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        this.add(title, BorderLayout.NORTH);

        JPanel kpiCardsPanel = new JPanel(new GridLayout(1, 4, 30, 0));
        kpiCardsPanel.setOpaque(false);
        kpiCardsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/admin/stats")).GET().build();
            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonObject stats = context.gson.fromJson(resp.body(), JsonObject.class);
                kpiCardsPanel.add(createKpiCard("Ingresos Totales", stats.get("ingresosTotales").getAsString(), "cash.png"));
                kpiCardsPanel.add(createKpiCard("Pedidos del Día", stats.get("pedidosDia").getAsString(), "clipboard.png"));
                kpiCardsPanel.add(createKpiCard("Nuevos Clientes", stats.get("nuevosClientes").getAsString(), "new-user.png"));
                kpiCardsPanel.add(createKpiCard("Empleados Activos", stats.get("empleadosActivos").getAsString(), "delivery-man.png"));
            } else { throw new Exception("Error al cargar stats: " + resp.statusCode()); }
        } catch (Exception e) {
            e.printStackTrace();
            kpiCardsPanel.add(createKpiCard("Ingresos Totales", "Error", "cash.png"));
            kpiCardsPanel.add(createKpiCard("Pedidos del Día", "Error", "clipboard.png"));
            kpiCardsPanel.add(createKpiCard("Nuevos Clientes", "Error", "new-user.png"));
            kpiCardsPanel.add(createKpiCard("Empleados Activos", "Error", "delivery-man.png"));
        }
        this.add(kpiCardsPanel, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, String value, String iconName) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));
        JLabel iconLabel = new JLabel();
        URL res = getClass().getResource("/icons/" + iconName);
        if (res != null) { iconLabel.setIcon(new ImageIcon(new ImageIcon(res).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH))); }
        card.add(iconLabel, BorderLayout.WEST);
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UIConstants.TITLE.deriveFont(24f));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.BASE.deriveFont(14f));
        titleLabel.setForeground(Color.GRAY);
        textPanel.add(valueLabel);
        textPanel.add(titleLabel);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }
}