package com.restaurante.pdv.model.repository;

import com.restaurante.pdv.model.entity.OpcaoAdicional;
import com.restaurante.pdv.model.entity.TipoOpcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA para gerenciamento de Opções Adicionais e Insumos.
 */
@Repository
public interface OpcaoAdicionalRepository extends JpaRepository<OpcaoAdicional, Long> {

    List<OpcaoAdicional> findByAtivoTrue();

    List<OpcaoAdicional> findByTipo(TipoOpcao tipo);

    List<OpcaoAdicional> findByTipoAndAtivoTrue(TipoOpcao tipo);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    @Query("SELECT o FROM OpcaoAdicional o JOIN o.produtos p WHERE p.id = :produtoId AND o.ativo = true")
    List<OpcaoAdicional> findByProdutoIdAndAtivoTrue(@Param("produtoId") Long produtoId);

    @Query("SELECT o FROM OpcaoAdicional o JOIN o.produtos p WHERE p.id = :produtoId")
    List<OpcaoAdicional> findByProdutoId(@Param("produtoId") Long produtoId);
}

