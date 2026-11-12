package com.restaurante.pdv.model.repository;

import com.restaurante.pdv.model.entity.Pedido;
import com.restaurante.pdv.model.entity.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório Spring Data JPA para pedidos.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByStatus(StatusPedido status);

    List<Pedido> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}

