package org.paq.UI;

import java.awt.*;
import java.io.InputStream;

public final class UIFonts {

    private static Font FONT_NORMAL, FONT_BOLD; // Renombrado: interRegular -> FONT_NORMAL, interBold -> FONT_BOLD

    static {
        try {
            InputStream reg = UIFonts.class.getResourceAsStream("/fonts/Inter-Regular.ttf");
            InputStream bold = UIFonts.class.getResourceAsStream("/fonts/Inter-Bold.ttf");

            // Cargar Inter
            if (reg != null) FONT_NORMAL = Font.createFont(Font.TRUETYPE_FONT, reg);
            if (bold != null) FONT_BOLD = Font.createFont(Font.TRUETYPE_FONT, bold);

            // Fallback si la carga falla
            if (FONT_NORMAL == null) FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 14);
            if (FONT_BOLD == null) FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);

        } catch (Exception e) {
            // Fallback en caso de excepción
            FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 14);
            FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
        }
    }

    public static Font getNormal(float size) { // Renombrado: interRegular -> getNormal
        return FONT_NORMAL.deriveFont(size);
    }

    public static Font getBold(float size) { // Renombrado: interBold -> getBold
        return FONT_BOLD.deriveFont(size);
    }

    private UIFonts() {}
}