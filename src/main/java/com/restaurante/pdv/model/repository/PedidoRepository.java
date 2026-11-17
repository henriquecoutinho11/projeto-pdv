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

    @org.springframework.data.jpa.repository.Query("SELECT MAX(p.numeroSenha) FROM Pedido p WHERE p.dataHora >= :inicio AND p.dataHora <= :fim")
    java.util.Optional<Integer> findMaxNumeroSenhaByDataHoraBetween(@org.springframework.data.repository.query.Param("inicio") LocalDateTime inicio,
                                                                  @org.springframework.data.repository.query.Param("fim") LocalDateTime fim);
}


