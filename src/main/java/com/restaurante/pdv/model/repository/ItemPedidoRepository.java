package com.restaurante.pdv.model.repository;

import com.restaurante.pdv.model.entity.ItemPedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório Spring Data JPA para itens de pedido com consultas de agregação para relatórios.
 */
@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    /**
     * Agrupa e ranqueia produtos mais vendidos em pedidos pagos dentro de um intervalo de datas.
     * Retorna: [produtoId (Long), nomeProduto (String), nomeCategoria (String), quantidadeVendida (Long/Integer), totalFaturado (BigDecimal)].
     */
    @Query("SELECT i.produto.id, i.produto.nome, c.nome, SUM(i.quantidade), SUM(i.subtotal) " +
           "FROM ItemPedido i " +
           "JOIN i.pedido p " +
           "JOIN i.produto prod " +
           "LEFT JOIN prod.categoria c " +
           "WHERE p.pago = true AND p.dataHora >= :inicio AND p.dataHora <= :fim " +
           "GROUP BY i.produto.id, i.produto.nome, c.nome " +
           "ORDER BY SUM(i.quantidade) DESC, SUM(i.subtotal) DESC")
    List<Object[]> findRankingProdutosMaisVendidos(@Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim,
                                                  Pageable pageable);

    /**
     * Agrupa e ranqueia todos os produtos vendidos em pedidos pagos dentro de um intervalo de datas (sem limite de paginação).
     */
    @Query("SELECT i.produto.id, i.produto.nome, c.nome, SUM(i.quantidade), SUM(i.subtotal) " +
           "FROM ItemPedido i " +
           "JOIN i.pedido p " +
           "JOIN i.produto prod " +
           "LEFT JOIN prod.categoria c " +
           "WHERE p.pago = true AND p.dataHora >= :inicio AND p.dataHora <= :fim " +
           "GROUP BY i.produto.id, i.produto.nome, c.nome " +
           "ORDER BY SUM(i.quantidade) DESC, SUM(i.subtotal) DESC")
    List<Object[]> findRankingProdutosMaisVendidosSemLimite(@Param("inicio") LocalDateTime inicio,
                                                           @Param("fim") LocalDateTime fim);
}
