package com.restaurante.pdv.service;

import com.restaurante.pdv.dto.request.OpcaoAdicionalRequestDTO;
import com.restaurante.pdv.dto.response.OpcaoAdicionalResponseDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.model.entity.OpcaoAdicional;
import com.restaurante.pdv.model.entity.Produto;
import com.restaurante.pdv.model.entity.TipoOpcao;
import com.restaurante.pdv.model.repository.OpcaoAdicionalRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
import com.restaurante.pdv.model.service.OpcaoAdicionalService;
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
class OpcaoAdicionalServiceTest {

    @Mock
    private OpcaoAdicionalRepository opcaoAdicionalRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private OpcaoAdicionalService opcaoAdicionalService;

    private OpcaoAdicional opcao;
    private Produto produto;

    @BeforeEach
    void setUp() {
        opcao = new OpcaoAdicional(1L, "Bacon Crocante", "Fatias de bacon", new BigDecimal("3.50"), TipoOpcao.ADICIONAL, false, true);
        produto = new Produto(10L, "X-Burguer", "Lanche artesanal", new BigDecimal("22.90"), 50, null, true, null);
    }

    @Test
    @DisplayName("Deve salvar opção com sucesso quando dados forem válidos")
    void deveSalvarOpcaoComSucesso() {
        OpcaoAdicionalRequestDTO dto = new OpcaoAdicionalRequestDTO("Cheddar Cremoso", "Cheddar", new BigDecimal("3.00"), TipoOpcao.ADICIONAL, false, true);

        when(opcaoAdicionalRepository.existsByNomeIgnoreCase("Cheddar Cremoso")).thenReturn(false);
        when(opcaoAdicionalRepository.save(any(OpcaoAdicional.class))).thenAnswer(i -> {
            OpcaoAdicional op = i.getArgument(0);
            op.setId(100L);
            return op;
        });

        OpcaoAdicionalResponseDTO resultado = opcaoAdicionalService.salvar(dto);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals("Cheddar Cremoso", resultado.getNome());
        assertEquals(new BigDecimal("3.00"), resultado.getPrecoExtra());
        verify(opcaoAdicionalRepository, times(1)).save(any(OpcaoAdicional.class));
    }

    @Test
    @DisplayName("Deve lançar NegocioException ao tentar salvar opção com nome duplicado")
    void deveLancarExcecaoComNomeDuplicado() {
        OpcaoAdicionalRequestDTO dto = new OpcaoAdicionalRequestDTO("Bacon Crocante", "Bacon", new BigDecimal("3.50"), TipoOpcao.ADICIONAL, false, true);

        when(opcaoAdicionalRepository.existsByNomeIgnoreCase("Bacon Crocante")).thenReturn(true);

        assertThrows(NegocioException.class, () -> opcaoAdicionalService.salvar(dto));
        verify(opcaoAdicionalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar NegocioException se preço for negativo")
    void deveLancarExcecaoComPrecoNegativo() {
        OpcaoAdicionalRequestDTO dto = new OpcaoAdicionalRequestDTO("Bacon", "Bacon", new BigDecimal("-2.00"), TipoOpcao.ADICIONAL, false, true);

        assertThrows(NegocioException.class, () -> opcaoAdicionalService.salvar(dto));
    }

    @Test
    @DisplayName("Deve buscar opções ativas")
    void deveBuscarOpcoesAtivas() {
        when(opcaoAdicionalRepository.findByAtivoTrue()).thenReturn(List.of(opcao));

        List<OpcaoAdicionalResponseDTO> resultado = opcaoAdicionalService.buscarAtivas();

        assertEquals(1, resultado.size());
        assertEquals("Bacon Crocante", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve buscar opções por tipo")
    void deveBuscarOpcoesPorTipo() {
        when(opcaoAdicionalRepository.findByTipoAndAtivoTrue(TipoOpcao.ADICIONAL)).thenReturn(List.of(opcao));

        List<OpcaoAdicionalResponseDTO> resultado = opcaoAdicionalService.buscarPorTipo(TipoOpcao.ADICIONAL);

        assertEquals(1, resultado.size());
        assertEquals(TipoOpcao.ADICIONAL, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("Deve vincular opção a um produto com sucesso")
    void deveVincularOpcaoAoProduto() {
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        when(opcaoAdicionalRepository.findById(1L)).thenReturn(Optional.of(opcao));

        opcaoAdicionalService.vincularAoProduto(10L, 1L);

        assertTrue(produto.getOpcoesDisponiveis().contains(opcao));
        verify(produtoRepository, times(1)).save(produto);
    }

    @Test
    @DisplayName("Deve desvincular opção de um produto com sucesso")
    void deveDesvincularOpcaoDoProduto() {
        produto.adicionarOpcao(opcao);
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        when(opcaoAdicionalRepository.findById(1L)).thenReturn(Optional.of(opcao));

        opcaoAdicionalService.desvincularDoProduto(10L, 1L);

        assertFalse(produto.getOpcoesDisponiveis().contains(opcao));
        verify(produtoRepository, times(1)).save(produto);
    }

    @Test
    @DisplayName("Deve desativar opção com sucesso")
    void deveDesativarOpcao() {
        when(opcaoAdicionalRepository.findById(1L)).thenReturn(Optional.of(opcao));

        opcaoAdicionalService.desativar(1L);

        assertFalse(opcao.getAtivo());
        verify(opcaoAdicionalRepository, times(1)).save(opcao);
    }
}

