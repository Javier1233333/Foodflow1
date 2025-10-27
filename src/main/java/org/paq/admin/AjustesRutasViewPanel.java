package org.paq.admin;

import com.google.gson.Gson; // Necesitas Gson para parsear
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.paq.UI.RoundedButton;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Vector;

public class AjustesRutasViewPanel extends JPanel {

    private final AdminViewContext context;
    private DefaultTableModel tableModel;
    private JTable rutasTable;

    public AjustesRutasViewPanel(AdminViewContext context) {
        super(new BorderLayout(0, 20));
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));
        initComponents();
        loadRutasData(); // Llama al método para cargar datos al inicio
    }

    private void initComponents() {
        JLabel title = new JLabel("Gestión de Rutas");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        this.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(20, 20, 20, 20)));

        String[] columnNames = {"ID RUTA", "Nombre", "Empleado (ID)", "Prioridad", "Estatus"};
        tableModel = new DefaultTableModel(columnNames, 0); // Asignación al campo de clase
        rutasTable = new JTable(tableModel);

        card.add(new JScrollPane(rutasTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        RoundedButton addButton = new RoundedButton("+ Add");
        // Lógica de botón: Abrir diálogo (usaremos el diálogo del otro panel)
        // addButton.addActionListener(e -> ??? );
        footer.add(addButton);
        card.add(footer, BorderLayout.CENTER); // Usamos CENTER temporalmente si BorderLayout.SOUTH no funciona bien con el JTable
        this.add(card, BorderLayout.CENTER);
    }

    /**
     * Carga las rutas desde el backend de forma asíncrona.
     */
    private void loadRutasData() {
        // Usamos un SwingWorker para no congelar la interfaz (IMPORTANTE en Swing)
        new SwingWorker<HttpResponse<String>, Void>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                // LLAMADA AL ENDPOINT: GET /api/admin/rutas/all
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8081/api/admin/rutas/all"))
                        .GET()
                        .build();
                return context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            }

            @Override
            protected void done() {
                try {
                    HttpResponse<String> resp = get();
                    if (resp.statusCode() == 200) {
                        // 1. Parsear el array de rutas
                        JsonArray rutas = context.gson.fromJson(resp.body(), JsonArray.class);

                        // 2. Limpiar tabla y añadir filas
                        tableModel.setRowCount(0); // Limpiar filas existentes
                        for (JsonElement rutaElement : rutas) {
                            JsonObject ruta = rutaElement.getAsJsonObject();
                            Vector<Object> row = new Vector<>();

                            // Llenar la fila con los campos de la entidad Ruta.java
                            row.add(ruta.get("id").getAsInt());
                            row.add(ruta.get("nombre").getAsString());
                            row.add(ruta.get("empleadoId").isJsonNull() ? "N/A" : ruta.get("empleadoId").getAsString());
                            row.add(ruta.get("prioridad").getAsString());
                            row.add(ruta.get("estatus").getAsString());

                            tableModel.addRow(row);
                        }
                    } else {
                        System.err.println("Error " + resp.statusCode() + " al cargar rutas: " + resp.body());
                        JOptionPane.showMessageDialog(AjustesRutasViewPanel.this,
                                "Fallo al cargar datos: " + resp.statusCode(), "Error API", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AjustesRutasViewPanel.this,
                            "Error de conexión con el servidor.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    // TODO: Añadir métodos para el diálogo de crear/editar ruta si es necesario
}