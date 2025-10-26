package org.paq.admin;

import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
// TODO: Añadir imports para conexión

public class AjustesRutasViewPanel extends JPanel {

    private final AdminViewContext context;

    public AjustesRutasViewPanel(AdminViewContext context) {
        super(new BorderLayout(0, 20));
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));
        initComponents();
    }

    private void initComponents() {
        JLabel title = new JLabel("Vista rutas");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        this.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));

        // TODO: Definir las columnas correctas y conectar al backend
        String[] columnNames = {"ID RUTA", "Nombre", "Empleado Asignado", "Estatus", "Accion"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable rutasTable = new JTable(tableModel);

        // Datos de ejemplo
        tableModel.addRow(new Object[]{"R-001", "Ruta Centro", "Juan Perez", "No iniciada", ""});
        tableModel.addRow(new Object[]{"R-002", "Ruta Sur", "Pedro Sanchez", "Completada", ""});

        card.add(new JScrollPane(rutasTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        RoundedButton addButton = new RoundedButton("+ Add");
        // Este botón podría usar el mismo diálogo que el Tablero de Rutas
        // addButton.addActionListener(e -> ??? ); // Quizás necesitemos pasar RutasViewPanel al contexto?
        footer.add(addButton);
        card.add(footer, BorderLayout.SOUTH);
        this.add(card, BorderLayout.CENTER);
    }
    // TODO: Añadir métodos para el diálogo de crear/editar ruta si es necesario
}