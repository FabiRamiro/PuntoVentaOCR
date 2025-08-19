package com.pos.puntoventaocr.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

public class ProductoTest {

    private Producto producto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setIdCategoria(1);
        categoria.setNombre("Electrónicos");

        producto = new Producto();
        producto.setIdProducto(1);
        producto.setNombre("Laptop Gaming");
        producto.setDescripcionCorta("Laptop para gaming de alta gama");
        producto.setPrecioCompra(new BigDecimal("800.00"));
        producto.setPrecioVenta(new BigDecimal("1200.00"));
        producto.setCantidadStock(10);
        producto.setCategoria(categoria);
        producto.setCodigoBarras("123456789");
        producto.setUnidadMedida("Pieza");
        producto.setRutaImagen("/images/laptop.jpg");
    }

    @Test
    void testCalcularGanancia() {
        BigDecimal ganancia = producto.calcularGanancia();
        assertEquals(new BigDecimal("400.00"), ganancia);
    }

    @Test
    void testCalcularPorcentajeGanancia() {
        double porcentaje = producto.calcularPorcentajeGanancia();
        assertEquals(50.0, porcentaje, 0.01); // 400/800 * 100 = 50%
    }

    @Test
    void testEsStockBajo() {
        // Stock actual es 10
        assertFalse(producto.esStockBajo(5)); // 10 > 5
        assertTrue(producto.esStockBajo(15)); // 10 <= 15
        assertTrue(producto.esStockBajo(10)); // 10 <= 10
        
        // Cambiar stock a 3
        producto.setCantidadStock(3);
        assertTrue(producto.esStockBajo(5)); // 3 <= 5
    }

    @Test
    void testGetImagenUrl() {
        String imagenUrl = producto.getImagenUrl();
        assertEquals("/images/laptop.jpg", imagenUrl);
        
        // Test when imagen is null
        producto.setRutaImagen(null);
        assertNull(producto.getImagenUrl());
    }

    @Test
    void testEstaAgotado() {
        assertFalse(producto.estaAgotado()); // Stock es 10
        
        producto.setCantidadStock(0);
        assertTrue(producto.estaAgotado()); // Stock es 0
    }

    @Test
    void testHasBajoStock() {
        // Stock mínimo por defecto es 5, stock actual es 10
        assertFalse(producto.hasBajoStock());
        
        producto.setCantidadStock(3);
        assertTrue(producto.hasBajoStock()); // 3 <= 5
        
        producto.setStockMinimo(2);
        assertFalse(producto.hasBajoStock()); // 3 > 2
    }

    @Test
    void testReducirStock() {
        int stockInicial = producto.getCantidadStock();
        producto.reducirStock(3);
        assertEquals(stockInicial - 3, producto.getCantidadStock());
        
        // Test error cuando no hay suficiente stock
        assertThrows(IllegalArgumentException.class, () -> {
            producto.reducirStock(20); // Intentar reducir más del disponible
        });
    }

    @Test
    void testAumentarStock() {
        int stockInicial = producto.getCantidadStock();
        producto.aumentarStock(5);
        assertEquals(stockInicial + 5, producto.getCantidadStock());
        
        // Test que no permite aumentar cantidad negativa
        producto.aumentarStock(0); // Esto no debería cambiar el stock
        assertEquals(stockInicial + 5, producto.getCantidadStock());
    }

    @Test
    void testCalcularMargenGananciaSinPrecioCompra() {
        producto.setPrecioCompra(BigDecimal.ZERO);
        double margen = producto.calcularMargenGanancia();
        assertEquals(0.0, margen, 0.01);
    }
}