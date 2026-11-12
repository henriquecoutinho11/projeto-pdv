package com.restaurante.pdv.service;

import com.restaurante.pdv.dto.ProdutoRequestDTO;
import com.restaurante.pdv.dto.ProdutoResponseDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.model.entity.Categoria;
import com.restaurante.pdv.model.entity.Produto;
import com.restaurante.pdv.model.repository.CategoriaRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
import com.restaurante.pdv.model.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Categoria categoria;
    private Produto produto;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Hambúrgueres", "Lanches artesanais", true);
        produto = new Produto(10L, "X-Burguer", "Delicioso x-burguer", new BigDecimal("22.90"), 50, "/img.jpg", true, categoria);
    }

    @Test
    @DisplayName("Deve salvar produto com sucesso quando dados forem válidos")
    void deveSalvarProdutoComSucesso() {
        ProdutoRequestDTO dto = new ProdutoRequestDTO("X-Burguer Novo", "Descrição", new BigDecimal("25.00"), 20, "/img.jpg", true, 1L);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.existsByNomeIgnoreCase("X-Burguer Novo")).thenReturn(false);
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        ProdutoResponseDTO resultado = produtoService.salvar(dto);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals("X-Burguer Novo", resultado.getNome());
        assertEquals(new BigDecimal("25.00"), resultado.getPreco());
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar produto sem categoria informada")
    void deveLancarExcecaoSemCategoria() {
        ProdutoRequestDTO dto = new ProdutoRequestDTO("X-Burguer", "Descrição", new BigDecimal("25.00"), 20, "/img.jpg", true, null);

        assertThrows(NegocioException.class, () -> produtoService.salvar(dto));
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar produto com preço zero ou negativo")
    void deveLancarExcecaoComPrecoInvalido() {
        ProdutoRequestDTO dtoZero = new ProdutoRequestDTO("X-Burguer", "Descrição", BigDecimal.ZERO, 20, "/img.jpg", true, 1L);
        ProdutoRequestDTO dtoNegativo = new ProdutoRequestDTO("X-Burguer", "Descrição", new BigDecimal("-10.00"), 20, "/img.jpg", true, 1L);

        assertThrows(NegocioException.class, () -> produtoService.salvar(dtoZero));
        assertThrows(NegocioException.class, () -> produtoService.salvar(dtoNegativo));
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar produto com categoria inexistente")
    void deveLancarExcecaoComCategoriaInexistente() {
        ProdutoRequestDTO dto = new ProdutoRequestDTO("X-Burguer", "Descrição", new BigDecimal("25.00"), 20, "/img.jpg", true, 999L);

        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> produtoService.salvar(dto));
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar estoque com sucesso quando quantidade for válida")
    void deveAtualizarEstoqueComSucesso() {
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));

        ProdutoResponseDTO resultado = produtoService.atualizarEstoque(10L, 80);

        assertNotNull(resultado);
        assertEquals(80, resultado.getQuantidadeEstoque());
        verify(produtoRepository, times(1)).save(produto);
    }

    @Test
    @DisplayName("Deve desativar produto com sucesso")
    void deveDesativarProdutoComSucesso() {
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));

        produtoService.desativar(10L);

        assertFalse(produto.getAtivo());
        verify(produtoRepository, times(1)).save(produto);
    }

    @Test
    @DisplayName("Deve listar apenas produtos ativos")
    void deveBuscarProdutosAtivos() {
        when(produtoRepository.findByAtivoTrue()).thenReturn(List.of(produto));

        List<ProdutoResponseDTO> ativos = produtoService.buscarAtivos();

        assertEquals(1, ativos.size());
        assertTrue(ativos.get(0).getAtivo());
    }
}
