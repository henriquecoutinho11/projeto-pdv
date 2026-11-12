package com.restaurante.pdv.model.service;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.response.ItemPedidoResponseDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.model.entity.*;
import com.restaurante.pdv.model.repository.OpcaoAdicionalRepository;
import com.restaurante.pdv.model.repository.PedidoRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Serviço responsável pelas regras de negócio de pedidos e motor de customização de lanches/combos (McDonald's Style).
 */
@Service
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final OpcaoAdicionalRepository opcaoAdicionalRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         OpcaoAdicionalRepository opcaoAdicionalRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.opcaoAdicionalRepository = opcaoAdicionalRepository;
    }

    /**
     * Valida os dados de customização enviados pelo cliente, recupera preços e regras estritas do banco de dados,
     * calcula o subtotal e retorna o DTO discriminado para exibição no Totem/Carrinho.
     *
     * Fórmula: subtotal = (precoBaseProduto + somaAdicionais + diferencaTamanho) * quantidade
     */
    public ItemPedidoResponseDTO calcularItem(CustomizacaoItemDTO dto) {
        ItemPedido item = montarItemPedido(dto);

        BigDecimal somaAdicionais = BigDecimal.ZERO;
        BigDecimal diferencaTamanho = BigDecimal.ZERO;
        List<String> remocoes = new ArrayList<>();

        for (ItemPedidoOpcao op : item.getOpcoes()) {
            if (op.getTipo() == TipoOpcao.TAMANHO_BATATA) {
                diferencaTamanho = diferencaTamanho.add(op.getPrecoExtra());
            } else if (op.getTipo() == TipoOpcao.ADICIONAL || op.getTipo() == TipoOpcao.BEBIDA) {
                somaAdicionais = somaAdicionais.add(op.getPrecoExtra());
            } else if (op.getTipo() == TipoOpcao.REMOCAO) {
                remocoes.add(op.getNomeOpcao());
            }
        }

        return ItemPedidoResponseDTO.deEntidade(item, somaAdicionais, diferencaTamanho, remocoes);
    }

    /**
     * Constrói e valida uma instância completa de ItemPedido com suas opções filhas (ItemPedidoOpcao).
     * Garante que preços vindos do front-end não sejam confiados e valida integridade contra o banco de dados.
     */
    public ItemPedido montarItemPedido(CustomizacaoItemDTO dto) {
        if (dto == null) {
            throw new NegocioException("Os dados de customização do item são obrigatórios.");
        }

        if (dto.getProdutoId() == null) {
            throw new NegocioException("O ID do produto é obrigatório.");
        }

        int quantidade = dto.getQuantidade() != null ? dto.getQuantidade() : 1;
        if (quantidade < 1) {
            throw new NegocioException("A quantidade mínima do item deve ser 1.");
        }

        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", dto.getProdutoId()));

        if (Boolean.FALSE.equals(produto.getAtivo())) {
            throw new NegocioException(String.format("O produto '%s' está inativo e não pode ser adicionado ao pedido.", produto.getNome()));
        }

        Set<OpcaoAdicional> opcoesDisponiveis = produto.getOpcoesDisponiveis();
        boolean validarVinculoProduto = opcoesDisponiveis != null && !opcoesDisponiveis.isEmpty();

        BigDecimal precoBaseProduto = produto.getPreco();
        BigDecimal somaAdicionais = BigDecimal.ZERO;
        BigDecimal diferencaTamanho = BigDecimal.ZERO;

        List<ItemPedidoOpcao> opcoesCriadas = new ArrayList<>();
        List<String> remocoesFormatadas = new ArrayList<>();

        // 1. Processar Adicionais Pagos (+Bacon, +Cheddar, etc.)
        if (dto.getAdicionaisIds() != null) {
            for (Long adicionalId : dto.getAdicionaisIds()) {
                OpcaoAdicional opcao = buscarEValidarOpcao(adicionalId, produto, validarVinculoProduto, opcoesDisponiveis);
                BigDecimal precoExtra = opcao.getPrecoExtra() != null ? opcao.getPrecoExtra() : BigDecimal.ZERO;
                somaAdicionais = somaAdicionais.add(precoExtra);

                ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(opcao, opcao.getNome(), precoExtra, TipoOpcao.ADICIONAL);
                opcoesCriadas.add(itemOpcao);
            }
        }

        // 2. Processar Tamanho de Acompanhamento / Batata em Combos
        if (dto.getTamanhoBatataId() != null) {
            OpcaoAdicional opcaoTamanho = buscarEValidarOpcao(dto.getTamanhoBatataId(), produto, validarVinculoProduto, opcoesDisponiveis);
            BigDecimal precoDiff = opcaoTamanho.getPrecoExtra() != null ? opcaoTamanho.getPrecoExtra() : BigDecimal.ZERO;
            diferencaTamanho = diferencaTamanho.add(precoDiff);

            ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(opcaoTamanho, opcaoTamanho.getNome(), precoDiff, TipoOpcao.TAMANHO_BATATA);
            opcoesCriadas.add(itemOpcao);
        }

        // 3. Processar Bebida em Combos
        if (dto.getBebidaId() != null) {
            OpcaoAdicional opcaoBebida = buscarEValidarOpcao(dto.getBebidaId(), produto, validarVinculoProduto, opcoesDisponiveis);
            BigDecimal precoBebida = opcaoBebida.getPrecoExtra() != null ? opcaoBebida.getPrecoExtra() : BigDecimal.ZERO;
            somaAdicionais = somaAdicionais.add(precoBebida);

            ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(opcaoBebida, opcaoBebida.getNome(), precoBebida, TipoOpcao.BEBIDA);
            opcoesCriadas.add(itemOpcao);
        }

        // 4. Processar Remoções de Ingredientes via IDs (ex: cebola, picles)
        if (dto.getRemocoesIds() != null) {
            for (Long remocaoId : dto.getRemocoesIds()) {
                OpcaoAdicional opcaoRemocao = buscarEValidarOpcao(remocaoId, produto, validarVinculoProduto, opcoesDisponiveis);

                if (!Boolean.TRUE.equals(opcaoRemocao.getPermiteRemocao()) && opcaoRemocao.getTipo() != TipoOpcao.REMOCAO) {
                    throw new NegocioException(String.format("A opção '%s' não é um ingrediente que permite remoção.", opcaoRemocao.getNome()));
                }

                String nomeFormatado = formatarTextoRemocao(opcaoRemocao.getNome());
                if (!remocoesFormatadas.contains(nomeFormatado)) {
                    remocoesFormatadas.add(nomeFormatado);
                }

                // Remoções de ingredientes NÃO alteram o valor (preço extra = 0.00)
                ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(opcaoRemocao, nomeFormatado, BigDecimal.ZERO, TipoOpcao.REMOCAO);
                opcoesCriadas.add(itemOpcao);
            }
        }

        // 5. Processar Remoções textuais diretas do front-end (ex: "Sem cebola", "Sem picles")
        if (dto.getRemocoes() != null) {
            for (String textoRemocao : dto.getRemocoes()) {
                if (textoRemocao != null && !textoRemocao.trim().isEmpty()) {
                    String nomeFormatado = formatarTextoRemocao(textoRemocao.trim());
                    if (!remocoesFormatadas.contains(nomeFormatado)) {
                        remocoesFormatadas.add(nomeFormatado);
                        ItemPedidoOpcao itemOpcao = new ItemPedidoOpcao(null, nomeFormatado, BigDecimal.ZERO, TipoOpcao.REMOCAO);
                        opcoesCriadas.add(itemOpcao);
                    }
                }
            }
        }

        // 6. Cálculo Estrito de Valores
        // precoUnitario = precoBaseProduto + somaAdicionais + diferencaTamanho
        BigDecimal precoUnitario = precoBaseProduto.add(somaAdicionais).add(diferencaTamanho);
        // subtotal = precoUnitario * quantidade
        BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));

        // 7. Montagem das Observações combinando remoções e notas livres
        String observacoesFinais = comporObservacoes(remocoesFormatadas, dto.getObservacoes());

        // 8. Construção da Entidade ItemPedido
        ItemPedido item = new ItemPedido();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(precoUnitario);
        item.setSubtotal(subtotal);
        item.setObservacoes(observacoesFinais);

        for (ItemPedidoOpcao opcao : opcoesCriadas) {
            item.adicionarOpcao(opcao);
        }

        return item;
    }

    private OpcaoAdicional buscarEValidarOpcao(Long opcaoId, Produto produto, boolean validarVinculo, Set<OpcaoAdicional> opcoesDisponiveis) {
        OpcaoAdicional opcao = opcaoAdicionalRepository.findById(opcaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("OpcaoAdicional", opcaoId));

        if (Boolean.FALSE.equals(opcao.getAtivo())) {
            throw new NegocioException(String.format("A opção '%s' está inativa.", opcao.getNome()));
        }

        if (validarVinculo && !opcoesDisponiveis.contains(opcao)) {
            throw new NegocioException(String.format("A opção '%s' não está disponível para o produto '%s'.", opcao.getNome(), produto.getNome()));
        }

        return opcao;
    }

    private String formatarTextoRemocao(String nome) {
        if (nome == null) return "";
        if (nome.toLowerCase().startsWith("sem ")) {
            return nome;
        }
        return "Sem " + nome;
    }

    private String comporObservacoes(List<String> remocoes, String observacoesLivres) {
        StringBuilder sb = new StringBuilder();

        if (remocoes != null && !remocoes.isEmpty()) {
            sb.append(String.join(", ", remocoes));
        }

        if (observacoesLivres != null && !observacoesLivres.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(". Obs: ");
            }
            sb.append(observacoesLivres.trim());
        }

        return sb.length() > 0 ? sb.toString() : null;
    }
}

