package org.paq.admin;

import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
// TODO: Añadir imports para conexión

public class AjustesUsuariosViewPanel extends JPanel {

    private final AdminViewContext context;

    public AjustesUsuariosViewPanel(AdminViewContext context) {
        super(new BorderLayout(0, 20));
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));
        initComponents();
    }

    private void initComponents() {
        JLabel title = new JLabel("Ajustes usuarios");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        this.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));

        String[] columnNames = {"ID USER", "Nombre", "Email", "Rol", "Accion"}; // Columnas actualizadas
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable usersTable = new JTable(tableModel);

        // TODO: Conectar a /api/admin/usuarios y añadir botones de acción reales
        // Usaremos datos de ejemplo por ahora
        tableModel.addRow(new Object[]{"1", "Juan Perez", "juan@mail.com", "empleado", ""});
        tableModel.addRow(new Object[]{"2", "Admin User", "admin@mail.com", "admin", ""});

        card.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        RoundedButton addButton = new RoundedButton("+ Add");
        // TODO: Conectar este botón a un diálogo para crear usuarios (similar a Rutas)
        // addButton.addActionListener(e -> showCreateUserDialog());
        footer.add(addButton);
        card.add(footer, BorderLayout.SOUTH);
        this.add(card, BorderLayout.CENTER);
    }
    // TODO: Añadir métodos para el diálogo de crear/editar usuario si es necesario
}