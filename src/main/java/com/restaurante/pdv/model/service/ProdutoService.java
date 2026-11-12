package com.restaurante.pdv.model.service;

import com.restaurante.pdv.dto.ProdutoRequestDTO;
import com.restaurante.pdv.dto.ProdutoResponseDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.model.entity.Categoria;
import com.restaurante.pdv.model.entity.Produto;
import com.restaurante.pdv.model.repository.CategoriaRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<ProdutoResponseDTO> listarTodos() {
        return produtoRepository.findAll().stream()
                .map(ProdutoResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    public List<ProdutoResponseDTO> buscarAtivos() {
        return produtoRepository.findByAtivoTrue().stream()
                .map(ProdutoResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = buscarEntidadePorId(id);
        return ProdutoResponseDTO.deEntidade(produto);
    }

    public List<ProdutoResponseDTO> buscarPorCategoria(Long categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new RecursoNaoEncontradoException("Categoria", categoriaId);
        }

        return produtoRepository.findByCategoriaId(categoriaId).stream()
                .map(ProdutoResponseDTO::deEntidade)
                .collect(Collectors.toList());
    }

    public Produto buscarEntidadePorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    @Transactional
    public ProdutoResponseDTO salvar(ProdutoRequestDTO dto) {
        validarRegrasDeNegocioProduto(dto);

        if (produtoRepository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new NegocioException(String.format("Já existe um produto cadastrado com o nome '%s'.", dto.getNome()));
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", dto.getCategoriaId()));

        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        produto.setImagemUrl(dto.getImagemUrl());
        produto.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        produto.setCategoria(categoria);

        Produto salvo = produtoRepository.save(produto);
        return ProdutoResponseDTO.deEntidade(salvo);
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto produto = buscarEntidadePorId(id);
        validarRegrasDeNegocioProduto(dto);

        if (produtoRepository.existsByNomeIgnoreCaseAndIdNot(dto.getNome(), id)) {
            throw new NegocioException(String.format("Já existe outro produto cadastrado com o nome '%s'.", dto.getNome()));
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", dto.getCategoriaId()));

        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        produto.setImagemUrl(dto.getImagemUrl());
        if (dto.getAtivo() != null) {
            produto.setAtivo(dto.getAtivo());
        }
        produto.setCategoria(categoria);

        Produto atualizado = produtoRepository.save(produto);
        return ProdutoResponseDTO.deEntidade(atualizado);
    }

    @Transactional
    public ProdutoResponseDTO atualizarEstoque(Long id, Integer novaQuantidade) {
        if (novaQuantidade == null || novaQuantidade < 0) {
            throw new NegocioException("A quantidade em estoque não pode ser negativa ou nula.");
        }

        Produto produto = buscarEntidadePorId(id);
        produto.setQuantidadeEstoque(novaQuantidade);
        Produto atualizado = produtoRepository.save(produto);
        return ProdutoResponseDTO.deEntidade(atualizado);
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = buscarEntidadePorId(id);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Transactional
    public void reativar(Long id) {
        Produto produto = buscarEntidadePorId(id);
        produto.setAtivo(true);
        produtoRepository.save(produto);
    }

    private void validarRegrasDeNegocioProduto(ProdutoRequestDTO dto) {
        if (dto.getCategoriaId() == null) {
            throw new NegocioException("É obrigatório vincular uma categoria ao produto.");
        }

        if (dto.getPreco() == null || dto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O preço do produto deve ser um valor positivo maior que zero.");
        }

        if (dto.getQuantidadeEstoque() == null || dto.getQuantidadeEstoque() < 0) {
            throw new NegocioException("A quantidade em estoque do produto não pode ser negativa.");
        }
    }
}
