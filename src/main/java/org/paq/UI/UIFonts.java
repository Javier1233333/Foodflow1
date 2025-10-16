package org.paq.UI;

import java.awt.*;
import java.io.InputStream;

public final class UIFonts {

    private static Font interRegular, interBold;

    static {
        try {
            InputStream reg = UIFonts.class.getResourceAsStream("/fonts/Inter-Regular.ttf");
            InputStream bold = UIFonts.class.getResourceAsStream("/fonts/Inter-Bold.ttf");
            if (reg != null) interRegular = Font.createFont(Font.TRUETYPE_FONT, reg);
            if (bold != null) interBold = Font.createFont(Font.TRUETYPE_FONT, bold);
        } catch (Exception e) {
            interRegular = new Font("SansSerif", Font.PLAIN, 14);
            interBold = new Font("SansSerif", Font.BOLD, 14);
        }
    }

    public static Font interRegular(float size) {
        return interRegular.deriveFont(size);
    }

    public static Font interBold(float size) {
        return interBold.deriveFont(size);
    }

    private UIFonts() {}
}
