package org.paq;

import io.javalin.Javalin;

public class ServerChafa {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(8080);

        System.out.println("✅ Servidor primitivo iniciado en http://localhost:8080");

        app.post("/api/auth/login", ctx -> {
            String body = ctx.body();
            System.out.println("Petición de login recibida: " + body);

            // --- LÓGICA DE ROLES ---
            // Simulación de dos tipos de usuario
            boolean isAdmin = body.contains("\"email\":\"admin@test.com\"")
                    && body.contains("\"password\":\"1234\"");

            boolean isEmpleado = body.contains("\"email\":\"empleado@test.com\"")
                    && body.contains("\"password\":\"1234\"");

            // Le decimos al cliente que la respuesta será en formato JSON
            ctx.contentType("application/json");

            if (isAdmin) {
                System.out.println("-> Usuario es ADMIN. Respondiendo con rol.");
                ctx.status(200);
                // Enviamos un JSON con el rol del usuario
                ctx.result("{\"message\":\"Login exitoso\", \"role\":\"admin\"}");
            } else if (isEmpleado) {
                System.out.println("-> Usuario es EMPLEADO. Respondiendo con rol.");
                ctx.status(200);
                ctx.result("{\"message\":\"Login exitoso\", \"role\":\"empleado\"}");
            } else {
                System.out.println("-> Credenciales incorrectas. Respondiendo con error 401.");
                ctx.status(401);
                ctx.result("{\"message\":\"Correo o contraseña incorrectos\"}");
            }
        });
    }
}