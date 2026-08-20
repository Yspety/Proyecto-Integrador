package com.cibertec.Proyecto_Integrador.service;

import com.cibertec.Proyecto_Integrador.entity.Orden;

/**
 * Arma el PDF de la boleta/factura de un pedido.
 *
 * <p>Interfaz aparte para que OrdenServiceImpl no cargue con la mecánica de dibujar
 * un PDF, y para poder cambiar de motor (o de plantilla) sin tocar el flujo de pedidos.
 */
public interface ComprobanteGenerator {

    /** Renderiza el comprobante. El caller ya validó que el pedido esté pagado. */
    byte[] generar(Orden order);
}
