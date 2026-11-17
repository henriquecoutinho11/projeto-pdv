package com.restaurante.pdv.service;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.response.ItemPedidoResponseDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.model.entity.*;
import com.restaurante.pdv.model.repository.OpcaoAdicionalRepository;
import com.restaurante.pdv.model.repository.PedidoRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
import com.restaurante.pdv.model.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private OpcaoAdicionalRepository opcaoAdicionalRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Produto burguer;
    private Produto combo;
    private OpcaoAdicional bacon;
    private OpcaoAdicional cheddar;
    private OpcaoAdicional cebola;
    private OpcaoAdicional picles;
    private OpcaoAdicional batataMedia;
    private OpcaoAdicional batataGrande;
    private OpcaoAdicional cocaCola;
    private OpcaoAdicional sucoLaranja;

    @BeforeEach
    void setUp() {
        Categoria catLanches = new Categoria(1L, "Hambúrgueres", "Lanches", true);
        Categoria catCombos = new Categoria(2L, "Combos", "Combos", true);

        burguer = new Produto(10L, "X-Burguer Tradicional", "Pão, carne e queijo", new BigDecimal("22.90"), 50, null, true, catLanches);
        combo = new Produto(20L, "Combo Especial", "Lanche, batata e bebida", new BigDecimal("42.00"), 30, null, true, catCombos);

        bacon = new OpcaoAdicional(1L, "Bacon Crocante", "Bacon fatiado", new BigDecimal("3.50"), TipoOpcao.ADICIONAL, false, true);
        cheddar = new OpcaoAdicional(2L, "Queijo Cheddar", "Cheddar cremoso", new BigDecimal("3.00"), TipoOpcao.ADICIONAL, false, true);
        cebola = new OpcaoAdicional(3L, "Cebola", "Cebola fresca", BigDecimal.ZERO, TipoOpcao.REMOCAO, true, true);
        picles = new OpcaoAdicional(4L, "Picles", "Picles em conserva", BigDecimal.ZERO, TipoOpcao.REMOCAO, true, true);

        batataMedia = new OpcaoAdicional(10L, "Batata Média", "Batata Média", BigDecimal.ZERO, TipoOpcao.TAMANHO_BATATA, false, true);
        batataGrande = new OpcaoAdicional(11L, "Batata Grande", "Batata Grande", new BigDecimal("4.00"), TipoOpcao.TAMANHO_BATATA, false, true);

        cocaCola = new OpcaoAdicional(20L, "Coca-Cola 350ml", "Lata", BigDecimal.ZERO, TipoOpcao.BEBIDA, false, true);
        sucoLaranja = new OpcaoAdicional(21L, "Suco Natural", "500ml", new BigDecimal("2.50"), TipoOpcao.BEBIDA, false, true);

        burguer.setOpcoesDisponiveis(new HashSet<>(Set.of(bacon, cheddar, cebola, picles)));
        combo.setOpcoesDisponiveis(new HashSet<>(Set.of(bacon, cheddar, cebola, picles, batataMedia, batataGrande, cocaCola, sucoLaranja)));
    }

    @Test
    @DisplayName("Deve calcular item com adicionais pagos somando corretamente ao valor unitário")
    void deveCalcularItemComAdicionaisPagos() {
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(10L);
        dto.setQuantidade(2);
        dto.setAdicionaisIds(List.of(1L, 2L)); // Bacon (+3.50), Cheddar (+3.00)

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(burguer));
        when(opcaoAdicionalRepository.findById(1L)).thenReturn(Optional.of(bacon));
        when(opcaoAdicionalRepository.findById(2L)).thenReturn(Optional.of(cheddar));

        ItemPedidoResponseDTO response = pedidoService.calcularItem(dto);

        assertNotNull(response);
        assertEquals(new BigDecimal("22.90"), response.getPrecoBaseProduto());
        assertEquals(new BigDecimal("6.50"), response.getSomaAdicionais());
        assertEquals(BigDecimal.ZERO, response.getDiferencaTamanho());
        // Preço unitário = 22.90 + 6.50 = 29.40
        assertEquals(new BigDecimal("29.40"), response.getPrecoUnitario());
        assertEquals(2, response.getQuantidade());
        // Subtotal = 29.40 * 2 = 58.80
        assertEquals(new BigDecimal("58.80"), response.getSubtotal());
        assertEquals(2, response.getOpcoes().size());
    }

    @Test
    @DisplayName("Remoções de ingredientes não devem alterar o valor e devem ficar registradas nas observações")
    void deveCalcularItemComRemocoesSemAlterarPreco() {
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(10L);
        dto.setQuantidade(1);
        dto.setRemocoesIds(List.of(3L, 4L)); // Cebola, Picles
        dto.setObservacoes("Carne ao ponto");

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(burguer));
        when(opcaoAdicionalRepository.findById(3L)).thenReturn(Optional.of(cebola));
        when(opcaoAdicionalRepository.findById(4L)).thenReturn(Optional.of(picles));

        ItemPedidoResponseDTO response = pedidoService.calcularItem(dto);

        assertNotNull(response);
        assertEquals(new BigDecimal("22.90"), response.getPrecoBaseProduto());
        assertEquals(BigDecimal.ZERO, response.getSomaAdicionais());
        assertEquals(new BigDecimal("22.90"), response.getPrecoUnitario());
        assertEquals(new BigDecimal("22.90"), response.getSubtotal());

        // Remoções gravadas na observação
        assertNotNull(response.getObservacoes());
        assertTrue(response.getObservacoes().contains("Sem Cebola"));
        assertTrue(response.getObservacoes().contains("Sem Picles"));
        assertTrue(response.getObservacoes().contains("Obs: Carne ao ponto"));
        assertEquals(2, response.getRemocoes().size());
    }

    @Test
    @DisplayName("Deve calcular combo McDonald's Style com diferença de tamanho de batata e upgrade de bebida")
    void deveCalcularComboComDiferencaDeTamanhoEBebida() {
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(20L); // Combo R$ 42.00
        dto.setQuantidade(3);
        dto.setAdicionaisIds(List.of(1L)); // Bacon (+3.50)
        dto.setTamanhoBatataId(11L); // Batata Grande (+4.00)
        dto.setBebidaId(21L); // Suco Natural (+2.50)
        dto.setRemocoesIds(List.of(3L)); // Sem cebola (R$ 0.00)

        when(produtoRepository.findById(20L)).thenReturn(Optional.of(combo));
        when(opcaoAdicionalRepository.findById(1L)).thenReturn(Optional.of(bacon));
        when(opcaoAdicionalRepository.findById(11L)).thenReturn(Optional.of(batataGrande));
        when(opcaoAdicionalRepository.findById(21L)).thenReturn(Optional.of(sucoLaranja));
        when(opcaoAdicionalRepository.findById(3L)).thenReturn(Optional.of(cebola));

        ItemPedidoResponseDTO response = pedidoService.calcularItem(dto);

        assertNotNull(response);
        // Preço base = 42.00
        assertEquals(new BigDecimal("42.00"), response.getPrecoBaseProduto());
        // Soma adicionais = 3.50 (bacon) + 2.50 (suco) = 6.00
        assertEquals(new BigDecimal("6.00"), response.getSomaAdicionais());
        // Diferença tamanho = 4.00 (batata grande)
        assertEquals(new BigDecimal("4.00"), response.getDiferencaTamanho());
        // Preço unitário = 42.00 + 6.00 + 4.00 = 52.00
        assertEquals(new BigDecimal("52.00"), response.getPrecoUnitario());
        // Subtotal = 52.00 * 3 = 156.00
        assertEquals(new BigDecimal("156.00"), response.getSubtotal());

        assertTrue(response.getObservacoes().contains("Sem Cebola"));
    }

    @Test
    @DisplayName("Preços são consultados do banco e nunca aceitos do cliente livremente")
    void deveValidarPrecosContraBancoDeDados() {
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(10L);
        dto.setQuantidade(1);
        dto.setAdicionaisIds(List.of(1L));

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(burguer));
        when(opcaoAdicionalRepository.findById(1L)).thenReturn(Optional.of(bacon));

        ItemPedido item = pedidoService.montarItemPedido(dto);

        // Preço do item extra deve vir estritamente de bacon.getPrecoExtra() do banco
        assertEquals(new BigDecimal("3.50"), item.getOpcoes().get(0).getPrecoExtra());
        assertEquals(new BigDecimal("26.40"), item.getSubtotal());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se produto não existir")
    void deveLancarExcecaoQuandoProdutoInexistente() {
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(999L);

        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> pedidoService.calcularItem(dto));
    }

    @Test
    @DisplayName("Deve lançar NegocioException se produto estiver inativo")
    void deveLancarExcecaoQuandoProdutoInativo() {
        burguer.setAtivo(false);
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(10L);

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(burguer));

        assertThrows(NegocioException.class, () -> pedidoService.calcularItem(dto));
    }

    @Test
    @DisplayName("Deve lançar NegocioException se opção estiver inativa")
    void deveLancarExcecaoQuandoOpcaoInativa() {
        bacon.setAtivo(false);
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(10L);
        dto.setAdicionaisIds(List.of(1L));

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(burguer));
        when(opcaoAdicionalRepository.findById(1L)).thenReturn(Optional.of(bacon));

        assertThrows(NegocioException.class, () -> pedidoService.calcularItem(dto));
    }

    @Test
    @DisplayName("Deve lançar NegocioException se opção não for permitida para o produto")
    void deveLancarExcecaoQuandoOpcaoNaoDisponivelParaOProduto() {
        // batataGrande não está nas opções disponíveis do burguer comum
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(10L);
        dto.setAdicionaisIds(List.of(11L)); // batataGrande

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(burguer));
        when(opcaoAdicionalRepository.findById(11L)).thenReturn(Optional.of(batataGrande));

        assertThrows(NegocioException.class, () -> pedidoService.calcularItem(dto));
    }

    @Test
    @DisplayName("Deve lançar NegocioException se tentar remover ingrediente que não permite remoção")
    void deveLancarExcecaoQuandoRemocaoNaoPermitida() {
        // bacon tem permiteRemocao = false e tipo = ADICIONAL
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(10L);
        dto.setRemocoesIds(List.of(1L)); // bacon

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(burguer));
        when(opcaoAdicionalRepository.findById(1L)).thenReturn(Optional.of(bacon));

        assertThrows(NegocioException.class, () -> pedidoService.calcularItem(dto));
    }

    @Test
    @DisplayName("Deve lançar NegocioException se quantidade for zero ou negativa")
    void deveLancarExcecaoQuandoQuantidadeInvalida() {
        CustomizacaoItemDTO dto = new CustomizacaoItemDTO();
        dto.setProdutoId(10L);
        dto.setQuantidade(0);

        assertThrows(NegocioException.class, () -> pedidoService.calcularItem(dto));
    }
}

