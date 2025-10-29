package org.paq.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AjustesUsuariosViewPanel extends JPanel {

    private final AdminViewContext context;
    private static final String BASE_URL = "http://localhost:8081/api/auth"; // API de Auth
    private DefaultTableModel tableModel;

    public AjustesUsuariosViewPanel(AdminViewContext context) {
        super(new BorderLayout(0, 20));
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));
        initComponents();
        loadUsersData(); // Cargar los datos al iniciar
    }

    private void initComponents() {
        JLabel title = new JLabel("Ajustes usuarios");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        this.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        String[] columnNames = {"ID USER", "Nombre", "Email", "Rol", "Accion"};
        this.tableModel = new DefaultTableModel(columnNames, 0); // Inicializado como campo de clase
        JTable usersTable = new JTable(tableModel);

        usersTable.setRowHeight(30);

        // Configurar el renderizador para la columna de acción
        setupButtonColumn(usersTable, 4);

        card.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        RoundedButton addButton = new RoundedButton("+ Add");
        addButton.addActionListener(e -> showCreateUserDialog()); // Conectar botón
        footer.add(addButton);
        card.add(footer, BorderLayout.SOUTH);
        this.add(card, BorderLayout.CENTER);
    }

    /**
     * Hace la consulta asíncrona a /api/auth/users y llena la tabla.
     */
    private void loadUsersData() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"Cargando usuarios...", "", "", "", ""});

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/users")).GET().build();

        // Petición asíncrona para no congelar la UI
        context.httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0); // Limpiar "Cargando..."
                        if (resp.statusCode() == 200) {
                            try {
                                JsonArray users = context.gson.fromJson(resp.body(), JsonArray.class);
                                for (JsonElement userEl : users) {
                                    JsonObject user = userEl.getAsJsonObject();

                                    String name = user.has("name") ? user.get("name").getAsString() : "";
                                    String lastName = user.has("lastName") ? user.get("lastName").getAsString() : "";

                                    tableModel.addRow(new Object[]{
                                            user.get("id").getAsLong(),
                                            name + " " + lastName,
                                            user.get("email").getAsString(),
                                            user.get("role").getAsString(),
                                            "Cambiar Rol" // Texto del botón de acción
                                    });
                                }
                            } catch (Exception e) {
                                tableModel.addRow(new Object[]{"Error de parseo de datos", "", "", "", ""});
                                e.printStackTrace();
                            }
                        } else {
                            tableModel.addRow(new Object[]{"Error al cargar: " + resp.statusCode(), "", "", "", ""});
                        }
                    });
                })
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        tableModel.addRow(new Object[]{"Error de conexión con el servidor", "", "", "", ""});
                        ex.printStackTrace();
                    });
                    return null;
                });
    }

    /**
     * Muestra un diálogo de ejemplo para crear un nuevo usuario.
     */
    private void showCreateUserDialog() {
        // En un proyecto real, aquí se abriría un JDialog con el formulario de registro.
        JOptionPane.showMessageDialog(this,
                "Lógica de 'Crear Usuario' pendiente de implementación.",
                "Próximamente",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // --- LÓGICA DE BOTÓN EN TABLA ---

    private void setupButtonColumn(JTable table, int column) {
        TableColumn actionColumn = table.getColumnModel().getColumn(column);
        actionColumn.setCellRenderer(new ButtonRenderer());
        // Aquí se usaría un ButtonEditor si se quisiera que el botón fuera clickable
    }

    /**
     * Renderiza el String en la celda como un botón.
     */
    class ButtonRenderer extends DefaultCellEditor implements ListCellRenderer, TableCellRenderer {
        private final JButton button;

        public ButtonRenderer() {
            // Se requiere pasar un componente para el constructor de DefaultCellEditor
            super(new JTextField());
            button = new RoundedButton("Cambiar Rol");
            button.setFocusPainted(false);
            button.setBackground(UIConstants.VERDE_700); // Color de acento
            button.setForeground(Color.WHITE);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            // Usamos un JPanel para contener el botón y centrarlo visualmente
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panel.add(button);

            // Asegura que el color de fondo coincida con la selección de la fila
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            return panel;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // Método requerido, sin uso en JTable
            return new JPanel();
        }
    }
}