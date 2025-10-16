package org.paq.UI;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Objects;

public final class FoodFlowThemes {
    public static void setup() {
        try { loadFont("/fonts/Inter-Regular.ttf"); } catch (Exception ignored) {}
        try { loadFont("/fonts/Inter-Bold.ttf");    } catch (Exception ignored) {}

        FlatAnimatedLafChange.showSnapshot();
        FlatLightLaf.setup();

        UIManager.put("defaultFont", new Font("Inter", Font.PLAIN, 14));
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.showButtons", false);

        // Acentos
        UIManager.put("Component.focusColor", UIConstants.VERDE_700);
        UIManager.put("Button.startBackground", UIConstants.VERDE_700);
        UIManager.put("Button.endBackground",   UIConstants.VERDE_700);

        // Tablas más “pro”
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0,1));
        UIManager.put("Table.selectionBackground", new Color(0xD1FAE5));

        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    private static void loadFont(String res) throws Exception {
        try (InputStream in = Objects.requireNonNull(FoodFlowThemes.class.getResourceAsStream(res))) {
            Font f = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
        }
    }
    private FoodFlowThemes() {}
}
