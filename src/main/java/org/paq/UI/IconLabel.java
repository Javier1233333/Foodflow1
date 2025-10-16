package org.paq.UI;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public final class IconLabel {

    private static JLabel loadIcon(String name) {
        URL res = IconLabel.class.getResource("/icons/" + name);
        JLabel l = new JLabel();
        if (res != null) {
            ImageIcon icon = new ImageIcon(res);
            Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            l.setIcon(new ImageIcon(scaled));
        }
        return l;
    }

    public static JLabel user() { return loadIcon("user.png"); }
    public static JLabel mail() { return loadIcon("email.png"); }
    public static JLabel lock() { return loadIcon("padlock.png"); }
    public static JLabel id()   { return loadIcon("id-card.png"); }

    private IconLabel() {}
}