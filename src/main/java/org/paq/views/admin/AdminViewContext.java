package org.paq.views.admin;

import com.google.gson.Gson;
import com.toedter.calendar.JCalendar;
import javax.swing.*;
import java.awt.*;
import java.net.http.HttpClient;

/**
 * Agrupa todos los objetos comunes que las vistas del admin necesitan.
 */
    public class AdminViewContext {
    public final HttpClient httpClient;
    public final Gson gson;
    public final CardLayout mainCards;
    public final JPanel mainPanel;
    public final JCalendar scheduleCalendar;

    public AdminViewContext(HttpClient httpClient, Gson gson, CardLayout mainCards, JPanel mainPanel, JCalendar scheduleCalendar) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.mainCards = mainCards;
        this.mainPanel = mainPanel;
        this.scheduleCalendar = scheduleCalendar;
    }
}