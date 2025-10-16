package org.paq;

import org.paq.UI.FoodFlowThemes;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            FoodFlowThemes.setup();               // <<—— aplicar tema
            new FoodFlowLogin().setVisible(true);
        });
    }
}