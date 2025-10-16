package org.paq.UI;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {
    public CardPanel() {
        super();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE),
                BorderFactory.createEmptyBorder(16,16,16,16)
        ));
    }
}