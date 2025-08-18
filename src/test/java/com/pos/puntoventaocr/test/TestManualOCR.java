package com.pos.puntoventaocr.test;

import com.pos.puntoventaocr.models.MotorOCR;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Programa de prueba manual para verificar que la extracción OCR
 * funciona correctamente con los formatos específicos de BBVA y NU
 * mencionados en el problema.
 */
public class TestManualOCR {

    public static void main(String[] args) {
        MotorOCR motorOCR = MotorOCR.getInstance();
        
        System.out.println("=== PRUEBA MANUAL DE EXTRACCIÓN OCR ===\n");
        
        // Probar comprobante BBVA con datos exactos del problema
        System.out.println("1. PROBANDO COMPROBANTE BBVA:");
        String textoBBVA = """
                BBVA MÉXICO
                COMPROBANTE DE TRANSFERENCIA SPEI
                Importe transferido: $67.28
                Clave de rastreo: MBAN010025081800655903904
                Fecha: 18 agosto 2025
                Cuenta origen: -3773
                Beneficiario: Osvaldo Fabian Ramiro Balboa
                """;
        
        testearExtraccion("BBVA", textoBBVA, motorOCR);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Probar comprobante NU con datos exactos del problema
        System.out.println("2. PROBANDO COMPROBANTE NU:");
        String textoNU = """
                nu
                COMPROBANTE DE TRANSFERENCIA
                Monto: $67.28
                Clave de rastreo: NU38KBHM4U8T8LFOFN92J3EALLAT
                Fecha: 18 AGO 2025
                Cuenta origen: ***0606
                Beneficiario: Osvaldo Fabian Ramiro Balboa
                """;
        
        testearExtraccion("NU", textoNU, motorOCR);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Probar compatibilidad con bancos existentes
        System.out.println("3. PROBANDO COMPATIBILIDAD CON BANCO EXISTENTE (SANTANDER):");
        String textoSantander = """
                SANTANDER
                COMPROBANTE DE TRANSFERENCIA
                FECHA: 15/08/2025
                MONTO: $1,250.00
                REFERENCIA: ABC123DEF456
                CUENTA DESTINO: ****1234
                BENEFICIARIO: COMERCIO XYZ SA
                """;
        
        testearExtraccion("SANTANDER", textoSantander, motorOCR);
        
        System.out.println("\n=== PRUEBA MANUAL COMPLETADA ===");
    }
    
    private static void testearExtraccion(String bancoEsperado, String texto, MotorOCR motorOCR) {
        System.out.println("Texto de entrada:");
        System.out.println(texto);
        System.out.println("\nResultados de extracción:");
        
        Map<String, Object> campos = motorOCR.detectarCampos(texto);
        
        // Verificar banco
        String bancoDetectado = (String) campos.get("bancoEmisor");
        System.out.println("✓ Banco: " + (bancoDetectado != null ? bancoDetectado : "NO DETECTADO"));
        if (bancoEsperado.equals(bancoDetectado)) {
            System.out.println("  ✅ CORRECTO");
        } else {
            System.out.println("  ❌ ESPERADO: " + bancoEsperado);
        }
        
        // Verificar monto
        BigDecimal monto = (BigDecimal) campos.get("montoDetectado");
        System.out.println("✓ Monto: " + (monto != null ? "$" + monto : "NO DETECTADO"));
        
        // Verificar referencia
        String referencia = (String) campos.get("referenciaOperacion");
        System.out.println("✓ Referencia: " + (referencia != null ? referencia : "NO DETECTADO"));
        
        // Verificar fecha
        LocalDateTime fecha = (LocalDateTime) campos.get("fechaTransferencia");
        System.out.println("✓ Fecha: " + (fecha != null ? fecha : "NO DETECTADO"));
        
        // Verificar cuenta remitente
        String cuenta = (String) campos.get("cuentaRemitente");
        System.out.println("✓ Cuenta remitente: " + (cuenta != null ? cuenta : "NO DETECTADO"));
        
        // Verificar beneficiario
        String beneficiario = (String) campos.get("nombreBeneficiario");
        System.out.println("✓ Beneficiario: " + (beneficiario != null ? beneficiario : "NO DETECTADO"));
        
        // Resumen
        int camposDetectados = 0;
        if (bancoDetectado != null) camposDetectados++;
        if (monto != null) camposDetectados++;
        if (referencia != null) camposDetectados++;
        if (fecha != null) camposDetectados++;
        if (cuenta != null) camposDetectados++;
        if (beneficiario != null) camposDetectados++;
        
        System.out.println("\nResumen: " + camposDetectados + "/6 campos detectados");
        if (camposDetectados >= 4) {
            System.out.println("🎉 EXTRACCIÓN EXITOSA");
        } else {
            System.out.println("⚠️  EXTRACCIÓN PARCIAL");
        }
    }
}