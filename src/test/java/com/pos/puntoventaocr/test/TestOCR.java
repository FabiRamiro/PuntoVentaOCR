package com.pos.puntoventaocr.test;

import com.pos.puntoventaocr.models.MotorOCR;

import java.util.Map;

/**
 * Clase de prueba para verificar las mejoras del OCR
 */
public class TestOCR {
    
    public static void main(String[] args) {
        MotorOCR motorOCR = MotorOCR.getInstance();
        
        // Casos de prueba para diferentes bancos
        testBBVA(motorOCR);
        testNuMexico(motorOCR);
        testGenerico(motorOCR);
    }
    
    private static void testBBVA(MotorOCR motorOCR) {
        System.out.println("=== Test BBVA ===");
        String textoBBVA = "BBVA MÉXICO\n" +
                "COMPROBANTE DE TRANSFERENCIA\n" +
                "FECHA: 15/08/2025 14:30:25\n" +
                "TRANSFERENCIA SPEI POR $1,250.00\n" +
                "FOLIO: 123456789\n" +
                "CUENTA DESTINO: ****1234\n" +
                "BENEFICIARIO: COMERCIO XYZ SA\n" +
                "CONCEPTO: PAGO VENTA";
        
        Map<String, Object> campos = motorOCR.detectarCampos(textoBBVA);
        
        System.out.println("Banco detectado: " + campos.get("bancoEmisor"));
        System.out.println("Monto detectado: " + campos.get("montoDetectado"));
        System.out.println("Referencia detectada: " + campos.get("referenciaOperacion"));
        System.out.println("Beneficiario detectado: " + campos.get("nombreBeneficiario"));
        System.out.println();
    }
    
    private static void testNuMexico(MotorOCR motorOCR) {
        System.out.println("=== Test Nu México ===");
        String textoNu = "NU MÉXICO\n" +
                "Enviaste $850.50\n" +
                "Para: TIENDA ABC\n" +
                "Clave: NUX789ABC\n" +
                "Fecha: 15/08/2025\n" +
                "Tu dinero llegó";
        
        Map<String, Object> campos = motorOCR.detectarCampos(textoNu);
        
        System.out.println("Banco detectado: " + campos.get("bancoEmisor"));
        System.out.println("Monto detectado: " + campos.get("montoDetectado"));
        System.out.println("Referencia detectada: " + campos.get("referenciaOperacion"));
        System.out.println("Beneficiario detectado: " + campos.get("nombreBeneficiario"));
        System.out.println();
    }
    
    private static void testGenerico(MotorOCR motorOCR) {
        System.out.println("=== Test Genérico (SANTANDER) ===");
        String textoGenerico = "SANTANDER\n" +
                "COMPROBANTE TRANSFERENCIA\n" +
                "FECHA: 15/08/2025\n" +
                "MONTO: $2,500.75\n" +
                "REF: STD123456XYZ\n" +
                "BENEFICIARIO: COMERCIO 123\n" +
                "CUENTA: ****5678";
        
        Map<String, Object> campos = motorOCR.detectarCampos(textoGenerico);
        
        System.out.println("Banco detectado: " + campos.get("bancoEmisor"));
        System.out.println("Monto detectado: " + campos.get("montoDetectado"));
        System.out.println("Referencia detectada: " + campos.get("referenciaOperacion"));
        System.out.println("Beneficiario detectado: " + campos.get("nombreBeneficiario"));
        System.out.println();
    }
}