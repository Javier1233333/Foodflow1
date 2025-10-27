package org.paq.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.paq.UI.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;

public class NominasHistoryViewPanel extends JPanel {

    private final AdminViewContext context;
    private DefaultTableModel tableModel;
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public NominasHistoryViewPanel(AdminViewContext context) {
        super(new BorderLayout(0, 20));
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
        loadNominasData(); // Carga datos al inicio
    }

    private void initComponents() {
        JLabel title = new JLabel("Historial y Gestión de Nóminas");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        this.add(title, BorderLayout.NORTH);

        // Definición de la tabla
        String[] columnNames = {"ID Nómina", "ID Empleado", "Período", "Bruto", "Neto", "Fecha", "Notas"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Esto hace que la tabla no sea editable por defecto
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable nominasTable = new JTable(tableModel);

        // Estilo de tabla (opcional)
        nominasTable.getTableHeader().setFont(UIConstants.BASE.deriveFont(Font.BOLD));

        // Panel contenedor de la tabla
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1), new EmptyBorder(10, 10, 10, 10)));
        card.add(new JScrollPane(nominasTable), BorderLayout.CENTER);

        this.add(card, BorderLayout.CENTER);
    }

    private void loadNominasData() {
        new SwingWorker<HttpResponse<String>, Void>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                // LLAMADA AL ENDPOINT: GET /api/admin/nominas/all
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8081/api/admin/nominas/all"))
                        .GET()
                        .build();
                return context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            }

            @Override
            protected void done() {
                try {
                    HttpResponse<String> resp = get();
                    if (resp.statusCode() == 200) {
                        JsonArray nominas = context.gson.fromJson(resp.body(), JsonArray.class);

                        // Limpiar y añadir filas
                        tableModel.setRowCount(0);

                        for (JsonElement element : nominas) {
                            JsonObject nomina = element.getAsJsonObject();
                            Vector<Object> row = new Vector<>();

                            // 1. Mapeo directo de los campos del modelo Nomina.java
                            row.add(nomina.get("id").getAsLong());
                            row.add(nomina.get("empleadoId").getAsLong());
                            row.add(nomina.get("periodoID").getAsInt());
                            row.add(moneyFormat.format(nomina.get("sueldoBruto").getAsDouble()));
                            row.add(moneyFormat.format(nomina.get("sueldoNeto").getAsDouble()));
                            row.add(nomina.get("fechaCreacion").getAsString().substring(0, 10)); // Solo fecha
                            row.add(nomina.get("notas").getAsString());

                            tableModel.addRow(row);
                        }
                    } else {
                        System.err.println("Error al cargar historial: " + resp.body());
                        JOptionPane.showMessageDialog(NominasHistoryViewPanel.this,
                                "Error " + resp.statusCode() + ": Fallo al cargar el historial.",
                                "Error de API", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(NominasHistoryViewPanel.this,
                            "Error de conexión con el servidor. (¿Está corriendo en 8081?)",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}