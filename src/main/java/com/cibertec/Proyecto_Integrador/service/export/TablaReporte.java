package com.cibertec.Proyecto_Integrador.service.export;

import java.util.List;

/**
 * Representación neutra de un reporte tabular, lista para renderizar.
 *
 * <p>Existe para no escribir seis exports a mano. Son tres reportes por dos formatos:
 * modelando la tabla una vez, quedan tres constructores de datos y dos renderizadores,
 * en lugar de seis funciones que se van desincronizando entre sí. Si mañana hay que
 * agregar CSV, es un renderizador más y ningún cambio en los reportes.
 *
 * @param numericas por columna: marca las que se alinean a la derecha y se escriben
 *                  como número real en Excel (para que se puedan sumar allá).
 * @param resumen   KPIs que van arriba de la tabla. Puede venir vacío.
 */
public record TablaReporte(
        String titulo,
        String subtitulo,
        List<String> encabezados,
        List<Boolean> numericas,
        List<List<String>> filas,
        List<Kpi> resumen) {

    public record Kpi(String label, String valor) {}

    public boolean esNumerica(int columna) {
        return columna < numericas.size() && Boolean.TRUE.equals(numericas.get(columna));
    }
}
