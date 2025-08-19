import com.pos.puntoventaocr.models.MotorOCR;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Comprehensive test for the new OCR functionality
 */
public class TestOCRComprehensive {
    public static void main(String[] args) {
        MotorOCR motor = MotorOCR.getInstance();
        
        System.out.println("=== Test 1: Comprobante BBVA ===");
        testBBVA(motor);
        
        System.out.println("\n=== Test 2: Comprobante NU ===");
        testNU(motor);
        
        System.out.println("\n=== Test 3: Comprobante Genérico ===");
        testGenerico(motor);
        
        System.out.println("\n=== Test 4: Múltiples formatos de monto ===");
        testMultiplesMontos(motor);
        
        System.out.println("\n=== Test 5: Validación con datos insuficientes ===");
        testDatosInsuficientes(motor);
        
        System.out.println("\n¡Todos los tests pasaron exitosamente!");
    }
    
    private static void testBBVA(MotorOCR motor) {
        String texto = "BBVA MÉXICO TRANSFERENCIA SPEI\nMONTO: $2,500.50\nREF: BBVA123456\nCUENTA: ****5678";
        Map<String, Object> campos = motor.detectarCampos(texto);
        
        assert campos.containsKey("bancoEmisor") : "Debe detectar banco BBVA";
        assert campos.containsKey("montoDetectado") : "Debe detectar monto";
        assert "BBVA".equals(campos.get("bancoEmisor")) : "Banco debe ser BBVA";
        
        BigDecimal monto = (BigDecimal) campos.get("montoDetectado");
        assert monto.compareTo(new BigDecimal("2500.50")) == 0 : "Monto debe ser 2500.50";
        
        System.out.println("✓ Test BBVA pasó");
    }
    
    private static void testNU(MotorOCR motor) {
        String texto = "NU BRASIL\nImporte transferido: $1,200.00\nReferencia: NU789012";
        Map<String, Object> campos = motor.detectarCampos(texto);
        
        assert campos.containsKey("montoDetectado") : "Debe detectar monto";
        
        BigDecimal monto = (BigDecimal) campos.get("montoDetectado");
        assert monto.compareTo(new BigDecimal("1200.00")) == 0 : "Monto debe ser 1200.00";
        
        System.out.println("✓ Test NU pasó");
    }
    
    private static void testGenerico(MotorOCR motor) {
        String texto = "BANCO DESCONOCIDO\nTRANSFERENCIA $750.25\nREF: GEN456789";
        Map<String, Object> campos = motor.detectarCampos(texto);
        
        assert campos.containsKey("montoDetectado") : "Debe detectar monto";
        
        BigDecimal monto = (BigDecimal) campos.get("montoDetectado");
        assert monto.compareTo(new BigDecimal("750.25")) == 0 : "Monto debe ser 750.25";
        
        System.out.println("✓ Test Genérico pasó");
    }
    
    private static void testMultiplesMontos(MotorOCR motor) {
        // Test múltiples formatos de monto
        String[] textos = {
            "TOTAL: $1,000.00",
            "Monto 500.50",
            "Importe transferido $2,350.75",
            "CANTIDAD: 125.00"
        };
        
        BigDecimal[] esperados = {
            new BigDecimal("1000.00"),
            new BigDecimal("500.50"),
            new BigDecimal("2350.75"),
            new BigDecimal("125.00")
        };
        
        for (int i = 0; i < textos.length; i++) {
            Map<String, Object> campos = motor.detectarCampos(textos[i]);
            assert campos.containsKey("montoDetectado") : "Debe detectar monto en formato " + (i+1);
            
            BigDecimal monto = (BigDecimal) campos.get("montoDetectado");
            assert monto.compareTo(esperados[i]) == 0 : "Monto debe ser " + esperados[i];
        }
        
        System.out.println("✓ Test Múltiples formatos de monto pasó");
    }
    
    private static void testDatosInsuficientes(MotorOCR motor) {
        String texto = "TEXTO SIN DATOS RELEVANTES";
        Map<String, Object> campos = motor.detectarCampos(texto);
        
        // Debe retornar un mapa, aunque esté vacío o con pocos datos
        assert campos != null : "Debe retornar un mapa no nulo";
        
        System.out.println("✓ Test Datos insuficientes pasó");
    }
}