package com.cibertec.Proyecto_Integrador.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.cibertec.Proyecto_Integrador.entity.Orden;
import com.cibertec.Proyecto_Integrador.entity.Usuario;
import com.cibertec.Proyecto_Integrador.entity.enums.EstadoOrden;

public interface OrdenRepository extends JpaRepository<Orden, Long>,
        JpaSpecificationExecutor<Orden> {

    /** Pedidos del cliente, más nuevos primero. */
    List<Orden> findByUserOrderByOrderDateDesc(Usuario user);

    /**
     * Pedido por id Y dueño. Buscar por ambos en la consulta (en vez de traer por id y
     * comparar después) hace que el IDOR sea imposible de olvidar: si no es tuyo, no
     * aparece, y el service devuelve 404.
     */
    Optional<Orden> findByIdAndUser(Long id, Usuario user);

    /**
     * Pedidos de un conjunto de estados dentro de [from, to). Base del reporte de ventas.
     * Límite superior EXCLUSIVO — ver OrdenSpecification.orderDateBetween.
     */
    List<Orden> findByStatusInAndOrderDateGreaterThanEqualAndOrderDateLessThan(
            Collection<EstadoOrden> statuses, Instant from, Instant to);
}
