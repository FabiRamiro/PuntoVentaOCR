import com.pos.puntoventaocr.models.MotorOCR;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Simple test for the new OCR functionality
 */
public class TestOCR {
    public static void main(String[] args) {
        // Test the procesarComprobanteGenerico method
        MotorOCR motor = MotorOCR.getInstance();
        
        // Simulate OCR extracted text
        String textoSimulado = "BBVA MÉXICO\n" +
                "COMPROBANTE DE TRANSFERENCIA\n" +
                "FECHA: 15/08/2025 14:30:25\n" +
                "MONTO: $1,250.00\n" +
                "REFERENCIA: ABC123DEF456\n" +
                "CUENTA DESTINO: ****1234\n" +
                "BENEFICIARIO: COMERCIO XYZ SA\n" +
                "CONCEPTO: PAGO VENTA";
        
        System.out.println("Probando detectarCampos con texto simulado...");
        Map<String, Object> campos = motor.detectarCampos(textoSimulado);
        
        System.out.println("Campos detectados:");
        for (Map.Entry<String, Object> entry : campos.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        
        // Test debug mode
        System.setProperty("ocr.debug.enabled", "true");
        System.out.println("\nProbando con modo debug habilitado...");
        campos = motor.detectarCampos(textoSimulado);
        
        System.out.println("Test completado exitosamente!");
    }
}