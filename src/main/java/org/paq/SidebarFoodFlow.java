package org.paq;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/** Sidebar moderna FoodFlow con ABSOLUTO, iconos y ancho fijo */
public class SidebarFoodFlow extends JPanel {

    // Paleta
    private final Color VERDE = new Color(14, 82, 29);
    private final Color BLANCO = Color.WHITE;
    private final Color TXT_W = new Color(245, 247, 246);
    private final Color TXT_G = new Color(220, 228, 224);

    // Métricas
    private final int SIDEBAR_W = 220;   // ancho fijo del sidebar
    private final int ITEM_H     = 38;
    private final int RADIUS     = 14;

    private final Font FONT_ITEM = new Font("Inter", Font.PLAIN, 14);
    private final Font FONT_HEAD = new Font("Inter", Font.BOLD, 15);

    private JLabel activo;
    private final JPanel panel;
    private final Map<String, JLabel> items = new LinkedHashMap<>();

    public SidebarFoodFlow() {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(SIDEBAR_W, 600));


        panel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(VERDE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        add(panel);
    }

    /** Llama tras añadir al frame para que tome alto total y se pinte */
    public void layoutSidebar(int height) {
        setBounds(0, 0, SIDEBAR_W, height);
        panel.setBounds(0, 0, SIDEBAR_W , height );
        construirUI();
    }

    private void construirUI() {
        panel.removeAll();

        // Logo pequeño/usuario arriba (opcional)
        JLabel top = new JLabel("Admin 1", loadIcon("/com/foodflow/assets/logo_foodflow.png", 20), JLabel.LEFT);
        top.setFont(FONT_HEAD);
        top.setForeground(TXT_W);
        top.setBounds(16, 16, panel.getWidth() - 32, 28);
        top.setIconTextGap(10);
        panel.add(top);

        int y = 60;
        y = addItem("Tablero Rutas",    "/ima/exit (3).png", y, true);
        y = addItem("Pagos y Nóminas",  "/ima/exit (3).png",   y, false);
        y = addItem("Horarios de empleados", "/com/foodflow/assets/ic_horarios.png", y, false);
        y = addItem("Inventarios",      "/com/foodflow/assets/ic_inventarios.png", y, false);
        y = addItem("Ajustes",          "/com/foodflow/assets/ic_ajustes.png", y, false);
        y = addItem("Empleados",        "/com/foodflow/assets/ic_empleados.png", y, false);

        // Salir abajo
        JLabel salir = makePill("Salir", "/com/foodflow/assets/ic_salir.png", false);
        int bottomY = panel.getHeight() - (ITEM_H + 35);
        salir.setBounds(14, bottomY, panel.getWidth() - 18, ITEM_H);
        salir.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { System.exit(0); }
        });
        panel.add(salir);

        panel.revalidate();
        panel.repaint();
    }

    private int addItem(String text, String iconPath, int y, boolean active) {
        JLabel item = makePill(text, iconPath, active);
        item.setBounds(14, y, panel.getWidth() - 28, ITEM_H);
        panel.add(item);
        items.put(text, item);
        return y + ITEM_H + 10;
    }

    private JLabel makePill(String text, String iconPath, boolean active) {
        JLabel pill = new JLabel(text, loadIcon(iconPath, 18), JLabel.LEFT) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = (this == activo);
                if (isActive) {
                    g2.setColor(BLANCO);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setOpaque(false);
        pill.setFont(FONT_ITEM);
        pill.setIconTextGap(10);
        pill.setForeground(active ? VERDE : TXT_W);
        if (active) activo = pill;
        pill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        pill.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { setActivo(pill); }
            @Override public void mouseEntered(MouseEvent e) {
                if (pill != activo) pill.setForeground(TXT_G);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (pill != activo) pill.setForeground(TXT_W);
            }
        });
        return pill;
    }

    private void setActivo(JLabel nuevo) {
        if (activo != null) activo.setForeground(TXT_W);
        activo = nuevo;
        activo.setForeground(VERDE);
        panel.repaint();
    }

    private ImageIcon loadIcon(String path, int size) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) return null;
            Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    /** Acceso al ancho fijo para usar en tu frame */
    public int getFixedWidth() { return SIDEBAR_W; }
}

