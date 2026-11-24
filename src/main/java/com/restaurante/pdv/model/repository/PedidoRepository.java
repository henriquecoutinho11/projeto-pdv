package com.restaurante.pdv.model.repository;

import com.restaurante.pdv.model.entity.Pedido;
import com.restaurante.pdv.model.entity.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para pedidos com consultas operacionais e analíticas.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByStatus(StatusPedido status);

    List<Pedido> findByStatusInOrderByDataHoraAsc(Collection<StatusPedido> statuses);

    List<Pedido> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    List<Pedido> findByPagoTrueAndDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT MAX(p.numeroSenha) FROM Pedido p WHERE p.dataHora >= :inicio AND p.dataHora <= :fim")
    Optional<Integer> findMaxNumeroSenhaByDataHoraBetween(@Param("inicio") LocalDateTime inicio,
                                                          @Param("fim") LocalDateTime fim);

    /**
     * Retorna a contagem total de pedidos pagos e o somatório faturado no intervalo especificado.
     * Retorna Array com [COUNT(p), COALESCE(SUM(p.valorTotal), 0)].
     */
    @Query("SELECT COUNT(p), COALESCE(SUM(p.valorTotal), 0) FROM Pedido p " +
           "WHERE p.pago = true AND p.dataHora >= :inicio AND p.dataHora <= :fim")
    List<Object[]> somarFaturamentoEPedidosPorPeriodo(@Param("inicio") LocalDateTime inicio,
                                                     @Param("fim") LocalDateTime fim);

    /**
     * Agrupa pedidos pagos por forma de pagamento no período.
     * Cada registro contém: [p.formaPagamento, COUNT(p), SUM(p.valorTotal)].
     */
    @Query("SELECT p.formaPagamento, COUNT(p), COALESCE(SUM(p.valorTotal), 0) FROM Pedido p " +
           "WHERE p.pago = true AND p.dataHora >= :inicio AND p.dataHora <= :fim " +
           "GROUP BY p.formaPagamento")
    List<Object[]> agruparPorFormaPagamentoNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                                    @Param("fim") LocalDateTime fim);

    /**
     * Agrupa todos os pedidos do período por status do pedido.
     * Cada registro contém: [p.status, COUNT(p), SUM(p.valorTotal)].
     */
    @Query("SELECT p.status, COUNT(p), COALESCE(SUM(p.valorTotal), 0) FROM Pedido p " +
           "WHERE p.dataHora >= :inicio AND p.dataHora <= :fim " +
           "GROUP BY p.status")
    List<Object[]> agruparPorStatusNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);

    /**
     * Retorna pedidos que possuem registro de prontidão (dataHoraPronto IS NOT NULL) no período.
     */
    @Query("SELECT p FROM Pedido p WHERE p.dataHoraPronto IS NOT NULL AND p.dataHora >= :inicio AND p.dataHora <= :fim")
    List<Pedido> findPedidosComTempoPreparoNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                                    @Param("fim") LocalDateTime fim);
}



