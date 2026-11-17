package com.restaurante.pdv.model.repository;

import com.restaurante.pdv.model.entity.CobrancaPix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para persistência e consulta de cobranças PIX.
 */
@Repository
public interface CobrancaPixRepository extends JpaRepository<CobrancaPix, Long> {

    /**
     * Recupera a cobrança PIX mais recente gerada para determinado pedido.
     */
    Optional<CobrancaPix> findTopByPedidoIdOrderByDataCriacaoDesc(Long pedidoId);

    /**
     * Recupera uma cobrança PIX através do identificador único da transação (txId).
     */
    Optional<CobrancaPix> findByTxId(String txId);

    /**
     * Retorna o histórico de cobranças geradas para o pedido.
     */
    List<CobrancaPix> findAllByPedidoIdOrderByDataCriacaoDesc(Long pedidoId);
}
