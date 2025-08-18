package com.pos.puntoventaocr.controllers;

import com.pos.puntoventaocr.utils.SessionManager;
import com.pos.puntoventaocr.models.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BienvenidaController {

    @FXML private Label lblBienvenidaUsuario;
    @FXML private Label lblBienvenidaRol;
    @FXML private Label lblBienvenidaFecha;

    private SessionManager sessionManager;

    public void initialize() {
        sessionManager = SessionManager.getInstance();
        configurarInformacionUsuario();
    }

    private void configurarInformacionUsuario() {
        Usuario usuario = sessionManager.getUsuarioActual();
        if (usuario != null) {
            lblBienvenidaUsuario.setText("Usuario: " + usuario.getNombreCompleto());
            lblBienvenidaRol.setText("Rol: " + usuario.getNombreRol());
            lblBienvenidaFecha.setText("Sesión iniciada: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } else {
            lblBienvenidaUsuario.setText("Usuario: No identificado");
            lblBienvenidaRol.setText("Rol: Sin rol");
            lblBienvenidaFecha.setText("Sesión: No válida");
        }
    }
}
