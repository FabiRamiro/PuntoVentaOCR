package com.pos.puntoventaocr.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MotorOCRTest {

    private MotorOCR motorOCR;

    @BeforeEach
    void setUp() {
        motorOCR = MotorOCR.getInstance();
    }

    @Test
    void testDetectarCamposBBVA() {
        // Simulación de texto extraído de un comprobante BBVA
        String textoBBVA = """
                BBVA MÉXICO
                COMPROBANTE DE TRANSFERENCIA
                Fecha: 18 agosto 2025
                Importe transferido: $67.28
                Clave de rastreo: MBAN010025081800655903904
                Cuenta origen: -3773
                Beneficiario: Osvaldo Fabian Ramiro Balboa
                """;

        Map<String, Object> campos = motorOCR.detectarCampos(textoBBVA);

        // Verificar que se detectó el banco
        assertEquals("BBVA", campos.get("bancoEmisor"));

        // Verificar que se detectó el monto
        BigDecimal montoEsperado = new BigDecimal("67.28");
        assertEquals(montoEsperado, campos.get("montoDetectado"));

        // Verificar que se detectó la referencia
        assertEquals("MBAN010025081800655903904", campos.get("referenciaOperacion"));

        // Verificar que se detectó la fecha
        LocalDateTime fechaEsperada = LocalDateTime.of(2025, 8, 18, 0, 0);
        assertEquals(fechaEsperada, campos.get("fechaTransferencia"));

        // Verificar que se detectó la cuenta remitente
        assertEquals("-3773", campos.get("cuentaRemitente"));

        // Verificar que se detectó el beneficiario
        assertEquals("Osvaldo Fabian Ramiro Balboa", campos.get("nombreBeneficiario"));
    }

    @Test
    void testDetectarCamposNU() {
        // Simulación de texto extraído de un comprobante NU
        String textoNU = """
                nu
                COMPROBANTE DE TRANSFERENCIA
                Fecha: 18 AGO 2025
                Monto: $67.28
                Clave de rastreo: NU38KBHM4U8T8LFOFN92J3EALLAT
                Cuenta origen: ***0606
                Beneficiario: Osvaldo Fabian Ramiro Balboa
                """;

        Map<String, Object> campos = motorOCR.detectarCampos(textoNU);

        // Verificar que se detectó el banco
        assertEquals("NU", campos.get("bancoEmisor"));

        // Verificar que se detectó el monto
        BigDecimal montoEsperado = new BigDecimal("67.28");
        assertEquals(montoEsperado, campos.get("montoDetectado"));

        // Verificar que se detectó la referencia
        assertEquals("NU38KBHM4U8T8LFOFN92J3EALLAT", campos.get("referenciaOperacion"));

        // Verificar que se detectó la fecha
        LocalDateTime fechaEsperada = LocalDateTime.of(2025, 8, 18, 0, 0);
        assertEquals(fechaEsperada, campos.get("fechaTransferencia"));

        // Verificar que se detectó la cuenta remitente
        assertEquals("***0606", campos.get("cuentaRemitente"));

        // Verificar que se detectó el beneficiario
        assertEquals("Osvaldo Fabian Ramiro Balboa", campos.get("nombreBeneficiario"));
    }

    @Test
    void testDetectarBancoNUCaseInsensitive() {
        String textoNuMinuscula = "nu banco digital";
        String textoNuMayuscula = "NU BANCO DIGITAL";
        String textoNuMixto = "Nu Banco Digital";

        Map<String, Object> campos1 = motorOCR.detectarCampos(textoNuMinuscula);
        Map<String, Object> campos2 = motorOCR.detectarCampos(textoNuMayuscula);
        Map<String, Object> campos3 = motorOCR.detectarCampos(textoNuMixto);

        assertEquals("NU", campos1.get("bancoEmisor"));
        assertEquals("NU", campos2.get("bancoEmisor"));
        assertEquals("NU", campos3.get("bancoEmisor"));
    }

    @Test
    void testDetectarFechaFormatosEspañoles() {
        String textoFechaCompleta = "Fecha: 18 agosto 2025";
        String textoFechaAbreviada = "Fecha: 18 AGO 2025";

        Map<String, Object> campos1 = motorOCR.detectarCampos(textoFechaCompleta);
        Map<String, Object> campos2 = motorOCR.detectarCampos(textoFechaAbreviada);

        LocalDateTime fechaEsperada = LocalDateTime.of(2025, 8, 18, 0, 0);
        assertEquals(fechaEsperada, campos1.get("fechaTransferencia"));
        assertEquals(fechaEsperada, campos2.get("fechaTransferencia"));
    }

    @Test
    void testDetectarReferenciasPorBanco() {
        String textoBBVA = "Clave de rastreo: MBAN010025081800655903904";
        String textoNU = "Clave de rastreo: NU38KBHM4U8T8LFOFN92J3EALLAT";

        Map<String, Object> campos1 = motorOCR.detectarCampos("BBVA\n" + textoBBVA);
        Map<String, Object> campos2 = motorOCR.detectarCampos("NU\n" + textoNU);

        assertEquals("MBAN010025081800655903904", campos1.get("referenciaOperacion"));
        assertEquals("NU38KBHM4U8T8LFOFN92J3EALLAT", campos2.get("referenciaOperacion"));
    }

    @Test
    void testDetectarCuentasRemitentesPorBanco() {
        String textoBBVA = "BBVA\nCuenta origen: -3773";
        String textoNU = "NU\nCuenta origen: ***0606";

        Map<String, Object> campos1 = motorOCR.detectarCampos(textoBBVA);
        Map<String, Object> campos2 = motorOCR.detectarCampos(textoNU);

        assertEquals("-3773", campos1.get("cuentaRemitente"));
        assertEquals("***0606", campos2.get("cuentaRemitente"));
    }

    @Test
    void testMontosPorEtiquetaEspecifica() {
        String textoBBVA = "BBVA\nImporte transferido: $67.28";
        String textoNU = "NU\nMonto: $67.28";

        Map<String, Object> campos1 = motorOCR.detectarCampos(textoBBVA);
        Map<String, Object> campos2 = motorOCR.detectarCampos(textoNU);

        BigDecimal montoEsperado = new BigDecimal("67.28");
        assertEquals(montoEsperado, campos1.get("montoDetectado"));
        assertEquals(montoEsperado, campos2.get("montoDetectado"));
    }

    @Test
    void testCompatibilidadConBancosExistentes() {
        // Verificar que otros bancos siguen funcionando
        String textoSantander = """
                SANTANDER
                COMPROBANTE DE TRANSFERENCIA
                FECHA: 15/08/2025
                MONTO: $1,250.00
                REFERENCIA: ABC123DEF456
                """;

        Map<String, Object> campos = motorOCR.detectarCampos(textoSantander);

        assertEquals("SANTANDER", campos.get("bancoEmisor"));
        assertNotNull(campos.get("montoDetectado"));
        assertNotNull(campos.get("fechaTransferencia"));
        assertNotNull(campos.get("referenciaOperacion"));
    }
}