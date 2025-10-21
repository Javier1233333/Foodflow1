package org.paq;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerChafa {

    // --- 1. Configuración de la Base de Datos ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SABORA"; // Revisa que sea tu BD
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ultimatexbox16"; // Revisa que sea tu contraseña

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(8080);

        System.out.println("✅ Servidor iniciado en http://localhost:8080");

        // --- ENDPOINT DE LOGIN ---
        app.post("/api/auth/login", ctx -> {
            JsonObject credenciales = gson.fromJson(ctx.body(), JsonObject.class);
            String email = credenciales.get("email").getAsString();
            String password = credenciales.get("password").getAsString();

            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "SELECT id, rol, password_hash FROM Usuarios WHERE email = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String passwordGuardada = rs.getString("password_hash");
                    // ADVERTENCIA: Compara en texto plano. Usa BCrypt en producción.
                    if (passwordGuardada != null && password.equals(passwordGuardada)) { // Check for null
                        String rol = rs.getString("rol");
                        int id = rs.getInt("id");
                        ctx.status(200).result(String.format(
                                "{\"message\":\"Login exitoso\", \"role\":\"%s\", \"id\":\"%d\"}", rol, id
                        ));
                    } else {
                        ctx.status(401).result("{\"message\":\"Correo o contraseña incorrectos\"}");
                    }
                } else {
                    ctx.status(401).result("{\"message\":\"Correo o contraseña incorrectos\"}");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                ctx.status(500).result("{\"error\":\"Error en la base de datos\"}");
            }
        });

        // --- ENDPOINT DE REGISTRO ---
        app.post("/api/auth/register", ctx -> {
            JsonObject nuevoUsuario = gson.fromJson(ctx.body(), JsonObject.class);
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String nombre = nuevoUsuario.get("nombre").getAsString();
                String apellidos = nuevoUsuario.get("apellidos").getAsString();
                String email = nuevoUsuario.get("email").getAsString();
                String password = nuevoUsuario.get("password").getAsString(); // Hashear en producción
                int codigoNum = (int)(Math.random() * 900000 + 100000);
                String codigoEmpresa = "EMP-" + codigoNum;

                String sql = "INSERT INTO Usuarios (nombre, apellidos, email, password_hash, codigo_empresa, rol) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, nombre);
                stmt.setString(2, apellidos);
                stmt.setString(3, email);
                stmt.setString(4, password);
                stmt.setString(5, codigoEmpresa);
                stmt.setString(6, "empleado"); // Registro público siempre es empleado
                stmt.executeUpdate();

                System.out.println("--- SIMULACIÓN DE CORREO ---");
                System.out.println("Para: " + email + ", Código: " + codigoEmpresa);
                System.out.println("-----------------------------");

                ctx.status(201).result("{\"message\":\"Registro exitoso\"}");
            } catch (SQLException e) {
                if (e.getSQLState().equals("23000")) { // Email duplicado
                    ctx.status(409).result("{\"error\":\"El correo electrónico ya está registrado\"}");
                } else {
                    e.printStackTrace();
                    ctx.status(500).result("{\"error\":\"Error en la base de datos\"}");
                }
            }
        });

        // --- ENDPOINT PERFIL EMPLEADO ---
        app.get("/api/perfil/{id}", ctx -> {
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "SELECT nombre, apellidos, email, codigo_empresa, rol FROM Usuarios WHERE id = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(ctx.pathParam("id")));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JsonObject perfil = new JsonObject();
                    perfil.addProperty("nombre", rs.getString("nombre"));
                    perfil.addProperty("apellidos", rs.getString("apellidos"));
                    perfil.addProperty("email", rs.getString("email"));
                    perfil.addProperty("codigo_empresa", rs.getString("codigo_empresa"));
                    perfil.addProperty("rol", rs.getString("rol"));
                    ctx.contentType("application/json").result(gson.toJson(perfil));
                } else {
                    ctx.status(404).result("{\"error\":\"Usuario no encontrado\"}");
                }
            } catch (SQLException | NumberFormatException e) { // Catch potential parse error
                e.printStackTrace();
                ctx.status(500).result("{\"error\":\"Error en el servidor\"}");
            }
        });

        // --- ENDPOINTS INVENTARIO ---
        app.get("/api/inventario", ctx -> {
            List<JsonObject> inventario = new ArrayList<>();
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "SELECT id, nombre_producto, cantidad FROM Inventario";
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    JsonObject item = new JsonObject();
                    item.addProperty("id", rs.getInt("id"));
                    item.addProperty("nombre", rs.getString("nombre_producto"));
                    item.addProperty("cantidad", rs.getInt("cantidad"));
                    inventario.add(item);
                }
                ctx.contentType("application/json").result(gson.toJson(inventario));
            } catch (SQLException e) { e.printStackTrace(); ctx.status(500).result("{\"error\":\"Error en la base de datos\"}"); }
        });

        app.post("/api/inventario/actualizar", ctx -> {
            try {
                JsonObject body = gson.fromJson(ctx.body(), JsonObject.class);
                int id = body.get("id").getAsInt();
                int nuevaCantidad = body.get("cantidad").getAsInt();
                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String sql = "UPDATE Inventario SET cantidad = ? WHERE id = ?";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setInt(1, nuevaCantidad);
                    stmt.setInt(2, id);
                    int filasAfectadas = stmt.executeUpdate();
                    if (filasAfectadas > 0) {
                        ctx.status(200).result("{\"message\":\"Inventario actualizado\"}");
                    } else {
                        ctx.status(404).result("{\"error\":\"Producto no encontrado\"}");
                    }
                } catch (SQLException e) { e.printStackTrace(); ctx.status(500).result("{\"error\":\"Error en la base de datos\"}"); }
            } catch (Exception e) { // Catch potential JSON errors
                e.printStackTrace();
                ctx.status(400).result("{\"error\":\"Solicitud inválida\"}");
            }
        });

        // --- ENDPOINTS PARA ADMIN ---

        // Obtener lista de usuarios
        app.get("/api/admin/usuarios", ctx -> {
            List<JsonObject> usuarios = new ArrayList<>();
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "SELECT id, nombre, apellidos, email, rol, codigo_empresa FROM Usuarios";
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    JsonObject usuario = new JsonObject();
                    usuario.addProperty("id", rs.getInt("id"));
                    usuario.addProperty("nombre", rs.getString("nombre") + " " + rs.getString("apellidos"));
                    usuario.addProperty("email", rs.getString("email"));
                    usuario.addProperty("rol", rs.getString("rol"));
                    usuario.addProperty("codigo_empresa", rs.getString("codigo_empresa"));
                    usuarios.add(usuario);
                }
                ctx.contentType("application/json").result(gson.toJson(usuarios));
            } catch (SQLException e) { e.printStackTrace(); ctx.status(500).result("{\"error\":\"Error en la base de datos\"}"); }
        });

        // Obtener estadísticas
        app.get("/api/admin/stats", ctx -> {
            // TODO: Hacer consultas reales a la BD
            JsonObject stats = new JsonObject();
            stats.addProperty("ingresosTotales", "$15,250.00");
            stats.addProperty("pedidosDia", 78);
            stats.addProperty("nuevosClientes", 12);
            stats.addProperty("empleadosActivos", 25);
            ctx.contentType("application/json").result(gson.toJson(stats));
        });

        // Crear usuario (desde Admin)
        app.post("/api/admin/crear-usuario", ctx -> {
            try {
                JsonObject nuevoUsuario = gson.fromJson(ctx.body(), JsonObject.class);
                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String nombre = nuevoUsuario.get("nombre").getAsString();
                    String apellidos = nuevoUsuario.get("apellidos").getAsString();
                    String email = nuevoUsuario.get("email").getAsString();
                    String password = nuevoUsuario.get("password").getAsString(); // Hashear en producción
                    String rol = nuevoUsuario.get("rol").getAsString();
                    int codigoNum = (int)(Math.random() * 900000 + 100000);
                    String codigoEmpresa = "EMP-" + codigoNum;

                    String sql = "INSERT INTO Usuarios (nombre, apellidos, email, password_hash, codigo_empresa, rol) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setString(1, nombre);
                    stmt.setString(2, apellidos);
                    stmt.setString(3, email);
                    stmt.setString(4, password);
                    stmt.setString(5, codigoEmpresa);
                    stmt.setString(6, rol);
                    stmt.executeUpdate();
                    ctx.status(201).result("{\"message\":\"Usuario creado por admin\"}");
                } catch (SQLException e) {
                    if (e.getSQLState().equals("23000")) {
                        ctx.status(409).result("{\"error\":\"El correo electrónico ya está registrado\"}");
                    } else {
                        e.printStackTrace();
                        ctx.status(500).result("{\"error\":\"Error en la base de datos\"}");
                    }
                }
            } catch (Exception e) { // Catch potential JSON errors
                e.printStackTrace();
                ctx.status(400).result("{\"error\":\"Solicitud inválida\"}");
            }
        });

        // --- ENDPOINTS PARA RUTAS (ADMIN) ---

        // Obtener rutas de hoy
        app.get("/api/admin/rutas/hoy", ctx -> {
            JsonObject respuesta = new JsonObject();
            JsonArray activas = new JsonArray();
            JsonArray completadas = new JsonArray();
            JsonObject progreso = new JsonObject();
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sqlActivas = "SELECT r.nombre, r.prioridad, r.estatus, r.fecha_creacion, u.codigo_empresa " +
                        "FROM Rutas r LEFT JOIN Usuarios u ON r.empleado_id = u.id " +
                        "WHERE r.estatus IN ('No iniciada', 'En progreso') AND DATE(r.fecha_creacion) = CURDATE()";
                try (PreparedStatement stmt = con.prepareStatement(sqlActivas)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        JsonObject ruta = new JsonObject();
                        ruta.addProperty("nombre", rs.getString("nombre"));
                        String empId = rs.getString("codigo_empresa");
                        ruta.addProperty("empId", empId == null ? "N/A" : empId);
                        ruta.addProperty("prioridad", rs.getString("prioridad"));
                        ruta.addProperty("estatus", rs.getString("estatus"));
                        ruta.addProperty("creado", rs.getTimestamp("fecha_creacion").toString());
                        activas.add(ruta);
                    }
                }
                String sqlCompletadas = "SELECT r.nombre, r.estatus, r.fecha_creacion, u.codigo_empresa " +
                        "FROM Rutas r LEFT JOIN Usuarios u ON r.empleado_id = u.id " +
                        "WHERE r.estatus = 'Completada' AND DATE(r.fecha_creacion) = CURDATE()";
                try (PreparedStatement stmt = con.prepareStatement(sqlCompletadas)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        JsonObject ruta = new JsonObject();
                        ruta.addProperty("nombre", rs.getString("nombre"));
                        String empId = rs.getString("codigo_empresa");
                        ruta.addProperty("empId", empId == null ? "N/A" : empId);
                        ruta.addProperty("estatus", rs.getString("estatus"));
                        ruta.addProperty("creado", rs.getTimestamp("fecha_creacion").toString());
                        completadas.add(ruta);
                    }
                }
                int totalRutasHoy = activas.size() + completadas.size();
                progreso.addProperty("completadas", completadas.size());
                progreso.addProperty("total", totalRutasHoy);
                respuesta.add("progreso", progreso);
                respuesta.add("rutas_activas", activas);
                respuesta.add("rutas_completadas", completadas);
                ctx.contentType("application/json").result(gson.toJson(respuesta));
            } catch (SQLException e) { e.printStackTrace(); ctx.status(500).result("{\"error\":\"Error en la base de datos\"}"); }
        });

        // Crear una nueva ruta
        app.post("/api/admin/rutas/crear", ctx -> {
            try {
                JsonObject nuevaRuta = gson.fromJson(ctx.body(), JsonObject.class);
                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String sql = "INSERT INTO Rutas (nombre, empleado_id, prioridad, estatus) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setString(1, nuevaRuta.get("nombre").getAsString());
                    // Manejar empleado_id opcional
                    if (nuevaRuta.has("empleado_id") && !nuevaRuta.get("empleado_id").isJsonNull()) {
                        stmt.setInt(2, nuevaRuta.get("empleado_id").getAsInt());
                    } else {
                        stmt.setNull(2, java.sql.Types.INTEGER);
                    }
                    stmt.setString(3, nuevaRuta.get("prioridad").getAsString());
                    stmt.setString(4, "No iniciada");
                    stmt.executeUpdate();
                    ctx.status(201).result("{\"message\":\"Ruta creada exitosamente\"}");
                } catch (SQLException e) { e.printStackTrace(); ctx.status(500).result("{\"error\":\"Error en la base de datos\"}"); }
            } catch (Exception e) { // Catch potential JSON errors
                e.printStackTrace();
                ctx.status(400).result("{\"error\":\"Solicitud inválida\"}");
            }
        });

        // --- ENDPOINTS PARA HORARIOS (ADMIN) ---

        // Obtener horarios de un día específico
        app.get("/api/admin/horarios-dia/{fecha}", ctx -> {
            String fecha = ctx.pathParam("fecha"); // "YYYY-MM-DD"
            JsonArray horariosDelDia = new JsonArray();
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "SELECT h.fecha_hora_inicio, h.fecha_hora_fin, h.estatus, u.nombre, u.apellidos " +
                        "FROM Horarios h JOIN Usuarios u ON h.empleado_id = u.id " +
                        "WHERE DATE(h.fecha_hora_inicio) = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, fecha);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    JsonObject horario = new JsonObject();
                    horario.addProperty("empleado", rs.getString("nombre") + " " + rs.getString("apellidos"));
                    String estatus = rs.getString("estatus");
                    if (estatus.equals("Descanso")) {
                        horario.addProperty("entrada", "DÍA DE DESCANSO");
                        horario.addProperty("salida", "DÍA DE DESCANSO");
                    } else {
                        // Avoid potential NPE if timestamps are NULL
                        java.sql.Timestamp inicioTS = rs.getTimestamp("fecha_hora_inicio");
                        java.sql.Timestamp finTS = rs.getTimestamp("fecha_hora_fin");
                        horario.addProperty("entrada", inicioTS != null ? inicioTS.toLocalDateTime().toLocalTime().toString() : "N/A");
                        horario.addProperty("salida", finTS != null ? finTS.toLocalDateTime().toLocalTime().toString() : "N/A");
                    }
                    horariosDelDia.add(horario);
                }
                ctx.contentType("application/json").result(gson.toJson(horariosDelDia));
            } catch (SQLException e) { e.printStackTrace(); ctx.status(500).result("{\"error\":\"Error en la base de datos\"}"); }
        });

        // Crear o actualizar un horario (Upsert)
        app.post("/api/admin/horarios/crear", ctx -> {
            try {
                JsonObject datos = gson.fromJson(ctx.body(), JsonObject.class);
                String empleadoId = datos.get("empleado_id").getAsString();
                String fecha = datos.get("fecha").getAsString(); // "YYYY-MM-DD"
                String estatus = datos.get("estatus").getAsString(); // "Laboral" o "Descanso"
                String entrada = datos.get("entrada").getAsString(); // "HH:MM"
                String salida = datos.get("salida").getAsString();   // "HH:MM"



                String inicio = fecha + " " + entrada + ":00"; // Format for MySQL TIMESTAMP
                String fin = fecha + " " + salida + ":00";

                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    // Borramos el horario existente
                    String sqlDelete = "DELETE FROM Horarios WHERE empleado_id = ? AND DATE(fecha_hora_inicio) = ?";
                    try (PreparedStatement stmtDel = con.prepareStatement(sqlDelete)) {
                        stmtDel.setString(1, empleadoId);
                        stmtDel.setString(2, fecha);
                        stmtDel.executeUpdate();
                    }
                    // Insertamos el nuevo registro
                    String sqlInsert = "INSERT INTO Horarios (empleado_id, fecha_hora_inicio, fecha_hora_fin, estatus) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmtIns = con.prepareStatement(sqlInsert)) {
                        stmtIns.setString(1, empleadoId);
                        stmtIns.setString(2, inicio);
                        stmtIns.setString(3, fin);
                        stmtIns.setString(4, estatus);
                        stmtIns.executeUpdate();
                    }
                    ctx.status(201).result("{\"message\":\"Horario guardado\"}");
                } catch (SQLException e) { e.printStackTrace(); ctx.status(500).result("{\"error\":\"Error en la base de datos\"}"); }
            } catch (Exception e) { // Catch potential JSON errors or missing fields
                e.printStackTrace();
                ctx.status(400).result("{\"error\":\"Solicitud inválida\"}");
            }
        });

    } // Fin del main()
} // Fin de la clase ServerChafa