package org.paq.views.admin;

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
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class InventarioViewPanel extends JPanel {

    private final AdminViewContext context;

    // CardLayout para cambiar entre "Tarjetas" y "Tabla"
    private final CardLayout viewSwitcherLayout = new CardLayout();
    private final JPanel viewSwitcherPanel = new JPanel(viewSwitcherLayout);

    private final RoundedButton toggleViewButton = new RoundedButton("Mostrar Tabla");
    private final RoundedButton newProductButton = new RoundedButton("Nuevo Producto");

    private final DefaultTableModel tableModel;

    public InventarioViewPanel(AdminViewContext context) {
        super(new BorderLayout(0, 20));
        this.context = context;
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(20, 0, 0, 0));


        String[] columnNames = {"ID", "Producto", "Cantidad", "Imagen (archivo)"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };


        initComponents();
        setupListeners();
        loadInventoryData(); // Ahora 'tableModel' ya no será 'null' aquí
    }

    private void initComponents() {
        // --- 1. Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Inventario");
        title.setFont(UIConstants.TITLE.deriveFont(32f));
        headerPanel.add(title, BorderLayout.WEST);

        // Botones en el header
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonGroup.setOpaque(false);
        buttonGroup.add(toggleViewButton);
        buttonGroup.add(newProductButton);
        headerPanel.add(buttonGroup, BorderLayout.EAST);

        this.add(headerPanel, BorderLayout.NORTH);

        // --- 2. Panel principal (con el CardLayout) ---
        // Se llenará en loadInventoryData()
        this.add(viewSwitcherPanel, BorderLayout.CENTER);
    }

    private void setupListeners() {
        // --- Listener para cambiar vistas ---
        toggleViewButton.addActionListener(e -> {
            if (toggleViewButton.getText().equals("Mostrar Tabla")) {
                viewSwitcherLayout.show(viewSwitcherPanel, "TABLA");
                toggleViewButton.setText("Mostrar Tarjetas");
            } else {
                viewSwitcherLayout.show(viewSwitcherPanel, "TARJETAS");
                toggleViewButton.setText("Mostrar Tabla");
            }
        });

        // --- Listener para nuevo producto ---
        newProductButton.addActionListener(e -> showCreateProductDialog());
    }

    /**
     * Carga los datos del backend y construye las vistas de Tarjeta y Tabla
     */
    private void loadInventoryData() {
        // Limpiar vistas anteriores
        viewSwitcherPanel.removeAll();
        tableModel.setRowCount(0);

        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/inventario")).GET().build();
            HttpResponse<String> resp = context.httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                JsonArray inventario = context.gson.fromJson(resp.body(), JsonArray.class);

                // Construir la vista de tabla y la vista de tarjetas
                JPanel cardView = createCardView(inventario);
                JPanel tableView = createTableView(inventario); // createTableView ahora llena tableModel

                viewSwitcherPanel.add(cardView, "TARJETAS");
                viewSwitcherPanel.add(tableView, "TABLA");

                viewSwitcherLayout.show(viewSwitcherPanel, "TARJETAS"); // Mostrar tarjetas por defecto
                toggleViewButton.setText("Mostrar Tabla"); // Estado inicial del botón

            } else {
                addErrorPanel("Error al cargar: " + resp.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorPanel("Error de conexión con el servidor.");
        }
    }

    private void addErrorPanel(String message) {
        JPanel errorPanel = new JPanel(new GridBagLayout());
        errorPanel.setOpaque(false);
        errorPanel.add(new JLabel(message));
        viewSwitcherPanel.add(errorPanel, "ERROR");
        viewSwitcherLayout.show(viewSwitcherPanel, "ERROR");
    }

    // --- Constructor de la Vista de Tabla ---
    private JPanel createTableView(JsonArray data) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Llenar el tableModel
        for (JsonElement itemEl : data) {
            JsonObject item = itemEl.getAsJsonObject();
            tableModel.addRow(new Object[]{
                    item.get("id").getAsInt(),
                    item.get("nombre").getAsString(),
                    item.get("cantidad").getAsInt(),
                    item.get("imagen_url").getAsString()
            });
        }

        JTable inventoryTable = new JTable(tableModel);
        inventoryTable.setFont(UIConstants.BASE.deriveFont(14f));
        inventoryTable.setRowHeight(28);

        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        return panel;
    }

    // --- Constructor de la Vista de Tarjetas ---
    private JPanel createCardView(JsonArray data) {
        JPanel wrapper = new JPanel(new BorderLayout()); // Panel principal
        wrapper.setOpaque(false);

        // Panel que contendrá las tarjetas
        JPanel panel = new JPanel(new GridLayout(0, 4, 20, 20)); // 4 columnas, espaciado
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (data.size() == 0) {
            wrapper.add(new JLabel("No hay productos en el inventario."), BorderLayout.NORTH);
        } else {
            for (JsonElement itemEl : data) {
                panel.add(createProductCard(itemEl.getAsJsonObject()));
            }
        }

        // Envolver el panel en un JScrollPane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    // --- Constructor de Tarjeta de Producto Individual ---
    private JPanel createProductCard(JsonObject item) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDE_SUAVE, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(220, 300)); // Tamaño fijo para la tarjeta

        // --- Carga de Imagen ---
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(180, 180));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        String imageName = item.get("imagen_url").getAsString();
        try {
            // Carga la imagen desde la carpeta resources/products/
            // ¡DEBES CREAR ESTA CARPETA y añadir las imágenes!
            URL res = getClass().getResource("/products/" + imageName);
            if (res == null) {
                res = getClass().getResource("/products/default.png"); // Imagen por defecto
            }
            if (res != null) {
                ImageIcon icon = new ImageIcon(res);
                Image scaled = icon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
            } else {
                imageLabel.setText("No Img");
            }
        } catch (Exception e) {
            imageLabel.setText("Error Img");
        }
        card.add(imageLabel, BorderLayout.CENTER);

        // --- Información del Producto ---
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nombre = new JLabel(item.get("nombre").getAsString());
        nombre.setFont(UIConstants.TITLE.deriveFont(16f));
        infoPanel.add(nombre);

        JLabel cantidad = new JLabel("Stock: " + item.get("cantidad").getAsInt());
        cantidad.setFont(UIConstants.BASE.deriveFont(14f));
        cantidad.setForeground(Color.GRAY);
        infoPanel.add(cantidad);

        card.add(infoPanel, BorderLayout.SOUTH);

        // TODO: Añadir botones de "Editar" / "Borrar" a la tarjeta si se desea

        return card;
    }

    // --- Diálogo para Crear Nuevo Producto ---
    private void showCreateProductDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Crear Nuevo Producto", true);
        JPanel formPanel = createProductFormPanel(dialog);
        dialog.setContentPane(formPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createProductFormPanel(JDialog dialog) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Nombre
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nombre Producto:"), gbc);
        gbc.gridx = 1; JTextField fNombre = new JTextField(20); panel.add(fNombre, gbc);

        // Fila 1: Cantidad
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Cantidad Inicial:"), gbc);
        gbc.gridx = 1; JTextField fCantidad = new JTextField(20); panel.add(fCantidad, gbc);

        // Fila 2: Nombre Imagen (con JFileChooser)
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Archivo Imagen:"), gbc);

        // Panel para el campo de texto y el botón
        JPanel imageChooserPanel = new JPanel(new BorderLayout(5, 0));
        imageChooserPanel.setOpaque(false);

        JTextField fImagen = new JTextField("default.png");
        fImagen.setEditable(false); // No se puede escribir
        fImagen.setBackground(Color.WHITE); // Fondo blanco aunque esté deshabilitado
        imageChooserPanel.add(fImagen, BorderLayout.CENTER);

        RoundedButton btnSeleccionar = new RoundedButton("Seleccionar...");
        imageChooserPanel.add(btnSeleccionar, BorderLayout.EAST);

        gbc.gridx = 1; panel.add(imageChooserPanel, gbc);

        // Acción del botón "Seleccionar..."
        btnSeleccionar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            // Filtro para solo mostrar imágenes
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "png", "gif", "jpeg"));
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                fImagen.setText(selectedFile.getName()); // Pone el nombre del archivo en el campo
            }
        });

        // Fila 3: Hint (Aviso)
        gbc.gridy = 3; gbc.gridx = 1;
        JLabel hint = new JLabel("<html>*Recuerda copiar la imagen seleccionada a<br>la carpeta 'src/main/resources/products'</html>");
        hint.setFont(UIConstants.BASE.deriveFont(10f));
        hint.setForeground(Color.GRAY);
        panel.add(hint, gbc);

        // Fila 4: Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        RoundedButton cancelButton = new RoundedButton("Cancelar");
        cancelButton.addActionListener(e -> dialog.dispose());
        RoundedButton createButton = new RoundedButton("Crear");

        createButton.addActionListener(e -> {

            String nombre = fNombre.getText().trim();
            String cantidadStr = fCantidad.getText().trim();
            String imagen = fImagen.getText().trim(); // <-- Esto sigue funcionando

            if (nombre.isEmpty() || cantidadStr.isEmpty() || imagen.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Todos los campos son obligatorios.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int cantidad;
            try {
                cantidad = Integer.parseInt(cantidadStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "La cantidad debe ser un número.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("nombre", nombre);
            body.addProperty("cantidad", cantidad);
            body.addProperty("imagen_url", imagen);

            try {
                HttpRequest postReq = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/inventario/crear"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(context.gson.toJson(body)))
                        .build();
                HttpResponse<String> postResp = context.httpClient.send(postReq, HttpResponse.BodyHandlers.ofString());

                if (postResp.statusCode() == 201) {
                    JOptionPane.showMessageDialog(dialog, "Producto creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadInventoryData();
                } else {
                    throw new Exception("Error del servidor: " + postResp.body());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error al crear el producto:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        gbc.gridy = 4; gbc.gridx = 1; panel.add(buttonPanel, gbc);

        return panel;
    }
}