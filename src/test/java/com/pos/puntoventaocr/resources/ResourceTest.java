package com.pos.puntoventaocr.resources;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

public class ResourceTest {

    @Test
    void testStylesCssExists() {
        InputStream cssFile = getClass().getResourceAsStream("/com/pos/puntoventaocr/styles.css");
        assertNotNull(cssFile, "styles.css file should exist in resources");
        
        try {
            cssFile.close();
        } catch (Exception e) {
            // Ignore
        }
    }

    @Test
    void testProductoViewFxmlExists() {
        InputStream fxmlFile = getClass().getResourceAsStream("/com/pos/puntoventaocr/producto-view.fxml");
        assertNotNull(fxmlFile, "producto-view.fxml file should exist in resources");
        
        try {
            fxmlFile.close();
        } catch (Exception e) {
            // Ignore
        }
    }
}