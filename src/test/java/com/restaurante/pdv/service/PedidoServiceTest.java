package com.restaurante.pdv.service;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.request.NovoPedidoDTO;
import com.restaurante.pdv.dto.response.ItemPedidoResponseDTO;
import com.restaurante.pdv.dto.response.PedidoResponseDTO;
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

    @Test
    @DisplayName("Deve processar novo pedido com sucesso, debitar estoque e calcular total estritamente no servidor")
    void deveProcessarNovoPedidoComSucessoEDebitarEstoque() {
        // Item 1: 2 burguers com bacon (+3.50 cada) -> unit: 22.90 + 3.50 = 26.40, subtotal: 52.80
        CustomizacaoItemDTO item1 = new CustomizacaoItemDTO(10L, 2);
        item1.setAdicionaisIds(List.of(1L)); // Bacon

        // Item 2: 1 combo com batata grande (+4.00) e suco (+2.50) -> unit: 42.00 + 6.50 = 48.50, subtotal: 48.50
        CustomizacaoItemDTO item2 = new CustomizacaoItemDTO(20L, 1);
        item2.setTamanhoBatataId(11L);
        item2.setBebidaId(21L);

        NovoPedidoDTO novoPedido = new NovoPedidoDTO(List.of(item1, item2), FormaPagamento.PIX, "Henrique", "12345678900");

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(burguer));
        when(produtoRepository.findById(20L)).thenReturn(Optional.of(combo));
        when(opcaoAdicionalRepository.findById(1L)).thenReturn(Optional.of(bacon));
        when(opcaoAdicionalRepository.findById(11L)).thenReturn(Optional.of(batataGrande));
        when(opcaoAdicionalRepository.findById(21L)).thenReturn(Optional.of(sucoLaranja));

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        PedidoResponseDTO response = pedidoService.processarNovoPedido(novoPedido);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(StatusPedido.RECEBIDO, response.getStatus());
        assertEquals(FormaPagamento.PIX, response.getFormaPagamento());
        assertEquals("Henrique", response.getNomeCliente());
        assertEquals("12345678900", response.getCpfCliente());
        assertEquals(2, response.getItens().size());
        assertEquals(3, response.getQuantidadeItens());

        // Total: 52.80 + 48.50 = 101.30
        assertEquals(new BigDecimal("101.30"), response.getValorTotal());

        // Validação de estoque debitado
        assertEquals(48, burguer.getQuantidadeEstoque()); // 50 - 2 = 48
        assertEquals(29, combo.getQuantidadeEstoque());   // 30 - 1 = 29
        verify(produtoRepository, times(1)).save(burguer);
        verify(produtoRepository, times(1)).save(combo);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar NegocioException quando estoque do produto for insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        burguer.setQuantidadeEstoque(1); // apenas 1 em estoque
        CustomizacaoItemDTO item = new CustomizacaoItemDTO(10L, 2); // solicita 2
        NovoPedidoDTO dto = new NovoPedidoDTO(List.of(item));

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(burguer));

        NegocioException ex = assertThrows(NegocioException.class, () -> pedidoService.processarNovoPedido(dto));
        assertTrue(ex.getMessage().contains("Estoque insuficiente"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar NegocioException quando pedido não contiver itens ou for nulo")
    void deveLancarExcecaoQuandoPedidoSemItens() {
        assertThrows(NegocioException.class, () -> pedidoService.processarNovoPedido(null));
        assertThrows(NegocioException.class, () -> pedidoService.processarNovoPedido(new NovoPedidoDTO(List.of())));
    }

    @Test
    @DisplayName("Deve buscar pedido por ID com sucesso")
    void deveBuscarPedidoPorIdComSucesso() {
        Pedido pedido = new Pedido(200L, 5, null, StatusPedido.RECEBIDO, FormaPagamento.CARTAO_CREDITO, new BigDecimal("45.00"), "Cliente", null);
        when(pedidoRepository.findById(200L)).thenReturn(Optional.of(pedido));

        PedidoResponseDTO response = pedidoService.buscarPorId(200L);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals(StatusPedido.RECEBIDO, response.getStatus());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao buscar pedido inexistente")
    void deveLancarExcecaoAoBuscarPedidoInexistente() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> pedidoService.buscarPorId(999L));
    }

    @Test
    @DisplayName("Deve buscar pedidos filtrados por status")
    void deveBuscarPedidosPorStatus() {
        Pedido p1 = new Pedido(1L, 1, null, StatusPedido.RECEBIDO, FormaPagamento.PIX, new BigDecimal("20.00"), null, null);
        Pedido p2 = new Pedido(2L, 2, null, StatusPedido.RECEBIDO, FormaPagamento.PIX, new BigDecimal("30.00"), null, null);

        when(pedidoRepository.findByStatus(StatusPedido.RECEBIDO)).thenReturn(List.of(p1, p2));

        List<PedidoResponseDTO> lista = pedidoService.buscarPorStatus(StatusPedido.RECEBIDO);

        assertEquals(2, lista.size());
        assertEquals(StatusPedido.RECEBIDO, lista.get(0).getStatus());
        assertEquals(StatusPedido.RECEBIDO, lista.get(1).getStatus());
    }

    @Test
    @DisplayName("Não deve permitir alterar status para EM_PREPARO quando pedido não estiver pago")
    void naoDevePermitirAlterarStatusParaPreparoSemPagamento() {
        Pedido pedido = new Pedido(300L, 10, null, StatusPedido.RECEBIDO, FormaPagamento.PIX, new BigDecimal("35.00"), "Cliente", null);
        pedido.setPago(Boolean.FALSE);

        when(pedidoRepository.findById(300L)).thenReturn(Optional.of(pedido));

        NegocioException ex = assertThrows(NegocioException.class, () ->
                pedidoService.atualizarStatus(300L, StatusPedido.EM_PREPARO)
        );

        assertTrue(ex.getMessage().contains("não pode ter status alterado para preparo na cozinha antes da confirmação do pagamento"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir alterar status para EM_PREPARO após confirmação do pagamento")
    void devePermitirAlterarStatusParaPreparoAposPagamento() {
        Pedido pedido = new Pedido(301L, 11, null, StatusPedido.RECEBIDO, FormaPagamento.PIX, new BigDecimal("35.00"), "Cliente", null);
        pedido.setPago(Boolean.TRUE);

        when(pedidoRepository.findById(301L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO response = pedidoService.atualizarStatus(301L, StatusPedido.EM_PREPARO);

        assertNotNull(response);
        assertEquals(StatusPedido.EM_PREPARO, response.getStatus());
        verify(pedidoRepository, times(1)).save(pedido);
    }
}

