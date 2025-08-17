package com.pos.puntoventaocr;

/**
 * Clase Launcher para evitar problemas con JavaFX en algunos entornos
 *
 * Esta clase actúa como un punto de entrada alternativo que no extiende Application,
 * lo cual puede ser útil para evitar problemas de classpath con JavaFX en ciertos entornos.
 */
public class Launcher {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  SISTEMA POS CON OCR");
        System.out.println("  Versión 1.0.0");
        System.out.println("  Iniciando aplicación...");
        System.out.println("===========================================");

        try {
            // Verificar que JavaFX esté disponible
            checkJavaFXAvailability();

            // Llamar al Main real
            Main.main(args);

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO: No se pudo iniciar la aplicación");
            System.err.println("Motivo: " + e.getMessage());
            e.printStackTrace();

            // Mostrar mensaje de ayuda
            mostrarAyudaError();

            System.exit(1);
        }
    }

    /**
     * Verifica que JavaFX esté disponible en el classpath
     */
    private static void checkJavaFXAvailability() {
        try {
            Class.forName("javafx.application.Application");
            System.out.println("✓ JavaFX detectado correctamente");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JavaFX no está disponible en el classpath. " +
                    "Asegúrese de tener JavaFX correctamente configurado.", e);
        }
    }

    /**
     * Muestra información de ayuda cuando hay errores
     */
    private static void mostrarAyudaError() {
        System.err.println("\n===========================================");
        System.err.println("  INFORMACIÓN DE AYUDA");
        System.err.println("===========================================");
        System.err.println("Si ve este error, puede ser debido a:");
        System.err.println("1. JavaFX no está instalado o configurado");
        System.err.println("2. La base de datos MySQL no está ejecutándose");
        System.err.println("3. La base de datos 'punto_venta_ocr' no existe");
        System.err.println("4. Problemas de permisos o configuración");
        System.err.println("\nPasos para solucionar:");
        System.err.println("1. Verificar que MySQL esté corriendo");
        System.err.println("2. Crear la base de datos con schema.sql");
        System.err.println("3. Verificar configuración en DatabaseConnection.java");
        System.err.println("4. Ejecutar: mvn clean compile");
        System.err.println("5. Ejecutar: mvn javafx:run");
        System.err.println("===========================================\n");
    }
}