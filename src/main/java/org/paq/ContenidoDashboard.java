package org.paq;

import org.paq.UI.PrimaryButton;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/* ======= ADMIN ======= */
class RepartoPanel extends JPanel {
    RepartoPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        JTextField busqueda = new JTextField(24);
        busqueda.putClientProperty("JTextField.placeholderText", "Buscar ruta o destino...");
        top.add(busqueda);
        top.add(new PrimaryButton("Nueva ruta"));
        add(top, BorderLayout.NORTH);

        String[] cols = {"Clave","Destino","Fecha","Asignado a","Estatus"};
        Object[][] data = {
                {"R-001","Zona Centro","Hoy 09:00","Juan","Asignada"},
                {"R-002","Zona Norte","Hoy 12:00","Ana","En camino"},
                {"R-003","Zona Sur","Mañana 08:30","Luis","Pendiente"}
        };
        JTable table = new JTable(new DefaultTableModel(data, cols));
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        foot.setOpaque(false);
        JButton eliminar = new JButton("Eliminar");
        JButton editar   = new JButton("Editar");
        PrimaryButton asignar = new PrimaryButton("Asignar repartidor");
        foot.add(eliminar); foot.add(editar); foot.add(asignar);
        add(foot, BorderLayout.SOUTH);
    }
}


class NominaPanel extends JPanel {
    NominaPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Nómina - cálculo/registro de pagos"), BorderLayout.NORTH);
        String[] cols = {"Empleado","Periodo","Monto","Estatus"};
        Object[][] data = {{"Juan","Semana 42","$2,350","Pagado"},{"Ana","Semana 42","$2,100","Pendiente"}};
        add(new JScrollPane(new JTable(new DefaultTableModel(data, cols))), BorderLayout.CENTER);
        add(new JButton("Generar nómina"), BorderLayout.SOUTH);
    }
}

class HorariosPanel extends JPanel {
    HorariosPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Horarios - asignación de turnos"), BorderLayout.NORTH);
        JTextArea ta = new JTextArea("Aquí va el calendario/tabla de turnos.\n(Implementa tu propio TableModel luego)");
        add(new JScrollPane(ta), BorderLayout.CENTER);
    }
}

class InventarioPanel extends JPanel {
    InventarioPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Inventario - insumos/equipos"), BorderLayout.NORTH);
        String[] cols = {"Ítem","Cantidad","Estado"};
        Object[][] data = {{"Caja térmica",8,"OK"},{"Moto",3,"Mantenimiento"}};
        add(new JScrollPane(new JTable(new DefaultTableModel(data, cols))), BorderLayout.CENTER);
        add(new JButton("Agregar ítem"), BorderLayout.SOUTH);
    }
}

class AjustesPanel extends JPanel {
    AjustesPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Ajustes - usuarios, permisos, parámetros"), BorderLayout.NORTH);
        add(new JTextArea("Configura aquí tu app (placeholders)"), BorderLayout.CENTER);
    }
}

class AyudaPanelAdmin extends JPanel {
    AyudaPanelAdmin() {
        setLayout(new BorderLayout());
        add(new JLabel("Ayuda (Admin) - bandeja de mensajes"), BorderLayout.NORTH);
        add(new JTextArea("Mensajes recibidos / enviar respuesta"), BorderLayout.CENTER);
    }
}

/* ======= EMPLEADO ======= */
class EntregasPanel extends JPanel {
    EntregasPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Entregas - rutas asignadas"), BorderLayout.NORTH);
        String[] cols = {"Ruta","Cliente","Dirección","Estatus"};
        Object[][] data = {{"R-001","Cafetería X","Av. Centro 123","En camino"},
                {"R-002","Restaurante Y","Calle Norte 45","Pendiente"}};
        add(new JScrollPane(new JTable(new DefaultTableModel(data, cols))), BorderLayout.CENTER);
        add(new JButton("Marcar entrega realizada"), BorderLayout.SOUTH);
    }
}

class NominaPanelEmpleado extends JPanel {
    NominaPanelEmpleado() {
        setLayout(new BorderLayout());
        add(new JLabel("Nómina (Empleado) - mis pagos"), BorderLayout.NORTH);
        String[] cols = {"Periodo","Monto","Estatus"};
        Object[][] data = {{"Semana 42","$2,350","Pagado"},{"Semana 41","$2,120","Pagado"}};
        add(new JScrollPane(new JTable(new DefaultTableModel(data, cols))), BorderLayout.CENTER);
    }
}

class HorarioPanelEmpleado extends JPanel {
    HorarioPanelEmpleado() {
        setLayout(new BorderLayout());
        add(new JLabel("Horario (Empleado) - mis turnos"), BorderLayout.NORTH);
        add(new JTextArea("Aquí puedes ver tus turnos asignados."), BorderLayout.CENTER);
    }
}

class AyudaPanelEmpleado extends JPanel {
    AyudaPanelEmpleado() {
        setLayout(new BorderLayout());
        add(new JLabel("Ayuda (Empleado) - contacto"), BorderLayout.NORTH);
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        JTextField asunto = new JTextField();
        JTextArea  mensaje = new JTextArea(5, 20);
        JButton enviar = new JButton("Enviar");
        form.add(new JLabel("Asunto"));  form.add(asunto);
        form.add(new JLabel("Mensaje")); form.add(new JScrollPane(mensaje));
        form.add(Box.createVerticalStrut(8)); form.add(enviar);
        add(form, BorderLayout.CENTER);
    }
}
