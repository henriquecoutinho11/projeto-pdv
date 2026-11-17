package com.restaurante.pdv.model.service;

import com.restaurante.pdv.dto.request.OpcaoAdicionalRequestDTO;
import com.restaurante.pdv.dto.response.OpcaoAdicionalResponseDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.model.entity.OpcaoAdicional;
import com.restaurante.pdv.model.entity.Produto;
import com.restaurante.pdv.model.entity.TipoOpcao;
import com.restaurante.pdv.model.repository.OpcaoAdicionalRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela gestão de Opções Adicionais, Insumos e Opções de Combos.
 */
@Service
@Transactional(readOnly = true)
public class OpcaoAdicionalService {

    private final OpcaoAdicionalRepository opcaoAdicionalRepository;
    private final ProdutoRepository produtoRepository;

    public OpcaoAdicionalService(OpcaoAdicionalRepository opcaoAdicionalRepository, ProdutoRepository produtoRepository) {
        this.opcaoAdicionalRepository = opcaoAdicionalRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<OpcaoAdicionalResponseDTO> listarTodas() {
        return opcaoAdicionalRepository.findAll().stream()
                .map(OpcaoAdicionalResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    public List<OpcaoAdicionalResponseDTO> buscarAtivas() {
        return opcaoAdicionalRepository.findByAtivoTrue().stream()
                .map(OpcaoAdicionalResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    public OpcaoAdicionalResponseDTO buscarPorId(Long id) {
        OpcaoAdicional opcao = buscarEntidadePorId(id);
        return OpcaoAdicionalResponseDTO.deEntidade(opcao);
    }

    public OpcaoAdicional buscarEntidadePorId(Long id) {
        return opcaoAdicionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("OpcaoAdicional", id));
    }

    public List<OpcaoAdicionalResponseDTO> buscarPorTipo(TipoOpcao tipo) {
        return opcaoAdicionalRepository.findByTipoAndAtivoTrue(tipo).stream()
                .map(OpcaoAdicionalResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    public List<OpcaoAdicionalResponseDTO> buscarPorProduto(Long produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new RecursoNaoEncontradoException("Produto", produtoId);
        }
        return opcaoAdicionalRepository.findByProdutoIdAndAtivoTrue(produtoId).stream()
                .map(OpcaoAdicionalResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    @Transactional
    public OpcaoAdicionalResponseDTO salvar(OpcaoAdicionalRequestDTO dto) {
        validarRegrasDeNegocioOpcao(dto);

        if (opcaoAdicionalRepository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new NegocioException(String.format("Já existe uma opção cadastrada com o nome '%s'.", dto.getNome()));
        }

        OpcaoAdicional opcao = new OpcaoAdicional();
        opcao.setNome(dto.getNome());
        opcao.setDescricao(dto.getDescricao());
        opcao.setPrecoExtra(dto.getPrecoExtra());
        opcao.setTipo(dto.getTipo());
        opcao.setPermiteRemocao(dto.getPermiteRemocao() != null ? dto.getPermiteRemocao() : (dto.getTipo() == TipoOpcao.REMOCAO));
        opcao.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

        OpcaoAdicional salvo = opcaoAdicionalRepository.save(opcao);
        return OpcaoAdicionalResponseDTO.deEntidade(salvo);
    }

    @Transactional
    public OpcaoAdicionalResponseDTO atualizar(Long id, OpcaoAdicionalRequestDTO dto) {
        OpcaoAdicional opcao = buscarEntidadePorId(id);
        validarRegrasDeNegocioOpcao(dto);

        if (opcaoAdicionalRepository.existsByNomeIgnoreCaseAndIdNot(dto.getNome(), id)) {
            throw new NegocioException(String.format("Já existe outra opção cadastrada com o nome '%s'.", dto.getNome()));
        }

        opcao.setNome(dto.getNome());
        opcao.setDescricao(dto.getDescricao());
        opcao.setPrecoExtra(dto.getPrecoExtra());
        opcao.setTipo(dto.getTipo());
        if (dto.getPermiteRemocao() != null) {
            opcao.setPermiteRemocao(dto.getPermiteRemocao());
        }
        if (dto.getAtivo() != null) {
            opcao.setAtivo(dto.getAtivo());
        }

        OpcaoAdicional atualizado = opcaoAdicionalRepository.save(opcao);
        return OpcaoAdicionalResponseDTO.deEntidade(atualizado);
    }

    @Transactional
    public void desativar(Long id) {
        OpcaoAdicional opcao = buscarEntidadePorId(id);
        opcao.setAtivo(false);
        opcaoAdicionalRepository.save(opcao);
    }

    @Transactional
    public void reativar(Long id) {
        OpcaoAdicional opcao = buscarEntidadePorId(id);
        opcao.setAtivo(true);
        opcaoAdicionalRepository.save(opcao);
    }

    @Transactional
    public void vincularAoProduto(Long produtoId, Long opcaoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", produtoId));
        OpcaoAdicional opcao = buscarEntidadePorId(opcaoId);

        produto.adicionarOpcao(opcao);
        produtoRepository.save(produto);
    }

    @Transactional
    public void desvincularDoProduto(Long produtoId, Long opcaoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", produtoId));
        OpcaoAdicional opcao = buscarEntidadePorId(opcaoId);

        produto.removerOpcao(opcao);
        produtoRepository.save(produto);
    }

    private void validarRegrasDeNegocioOpcao(OpcaoAdicionalRequestDTO dto) {
        if (dto.getTipo() == null) {
            throw new NegocioException("O tipo da opção é obrigatório.");
        }
        if (dto.getPrecoExtra() == null || dto.getPrecoExtra().compareTo(BigDecimal.ZERO) < 0) {
            throw new NegocioException("O preço extra da opção não pode ser nulo ou negativo.");
        }
    }
}

