package org.paq.UI;

import java.awt.*;

public final class UIConstants {
    // Paleta de colores (basada en tu Figma)
    public static final Color VERDE_700   = new Color(0x1E7A2E);   // tono más “Figma”
    public static final Color VERDE_800   = new Color(0x166225);
    public static final Color TEXTO_SEC   = new Color(0x9CA3AF);   // placeholder gris Figma
    public static final Color BORDE_SUAVE = new Color(0xD1D5DB);   // gris del borde
    public static final Color FONDO       = Color.WHITE;

    // Usamos los nuevos métodos getNormal() y getBold()
    public static final Font BASE   = UIFonts.getNormal(14f);
    public static final Font TITLE  = UIFonts.getBold(18f);
    public static final Font LOGO   = UIFonts.getBold(26f);

    private UIConstants() {}
}
