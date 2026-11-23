package com.restaurante.pdv.service;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.request.NovoPedidoDTO;
import com.restaurante.pdv.dto.response.PedidoResponseDTO;
import com.restaurante.pdv.event.PedidoCriadoEvent;
import com.restaurante.pdv.event.PedidoStatusAlteradoEvent;
import com.restaurante.pdv.model.entity.*;
import com.restaurante.pdv.model.repository.OpcaoAdicionalRepository;
import com.restaurante.pdv.model.repository.PedidoRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
import com.restaurante.pdv.model.service.PedidoService;
import com.restaurante.pdv.model.service.SenhaSequencialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Padrão Observer no PedidoService")
class PedidoServiceObserverTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private OpcaoAdicionalRepository opcaoAdicionalRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SenhaSequencialService senhaSequencialService;
    private PedidoService pedidoService;
    private Produto xSalada;

    @BeforeEach
    void setUp() {
        senhaSequencialService = new SenhaSequencialService(pedidoRepository);
        pedidoService = new PedidoService(
                pedidoRepository,
                produtoRepository,
                opcaoAdicionalRepository,
                senhaSequencialService,
                eventPublisher
        );

        Categoria categoria = new Categoria(1L, "Lanches", "Lanches artesanais", true);
        xSalada = new Produto(10L, "X-Salada", "Pão e salada", new BigDecimal("25.00"), 100, null, true, categoria);
    }

    @Test
    @DisplayName("Deve publicar PedidoCriadoEvent via ApplicationEventPublisher ao processar novo pedido")
    void devePublicarPedidoCriadoEventAoProcessarNovoPedido() {
        NovoPedidoDTO dto = new NovoPedidoDTO();
        dto.setFormaPagamento(FormaPagamento.PIX);
        dto.setNomeCliente("João Pedro");

        CustomizacaoItemDTO itemDto = new CustomizacaoItemDTO();
        itemDto.setProdutoId(10L);
        itemDto.setQuantidade(2);
        dto.setItens(List.of(itemDto));

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(xSalada));
        when(pedidoRepository.findMaxNumeroSenhaByDataHoraBetween(any(), any())).thenReturn(Optional.of(41));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(99L);
            return p;
        });

        PedidoResponseDTO response = pedidoService.processarNovoPedido(dto);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals(42, response.getNumeroSenha());

        ArgumentCaptor<PedidoCriadoEvent> captor = ArgumentCaptor.forClass(PedidoCriadoEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        PedidoCriadoEvent eventoPublicado = captor.getValue();
        assertEquals(99L, eventoPublicado.getPedidoId());
        assertEquals(42, eventoPublicado.getNumeroSenha());
        assertEquals(new BigDecimal("50.00"), eventoPublicado.getValorTotal());
        assertEquals("João Pedro", eventoPublicado.getNomeCliente());
        assertNotNull(eventoPublicado.getDataHora());
    }

    @Test
    @DisplayName("Deve publicar PedidoStatusAlteradoEvent ao atualizar o status do pedido")
    void devePublicarPedidoStatusAlteradoEventAoAtualizarStatus() {
        Pedido pedidoExistente = new Pedido();
        pedidoExistente.setId(88L);
        pedidoExistente.setNumeroSenha(15);
        pedidoExistente.setStatus(StatusPedido.RECEBIDO);
        pedidoExistente.setPago(true);

        when(pedidoRepository.findById(88L)).thenReturn(Optional.of(pedidoExistente));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO response = pedidoService.atualizarStatus(88L, StatusPedido.EM_PREPARO);

        assertNotNull(response);
        assertEquals(StatusPedido.EM_PREPARO, response.getStatus());

        ArgumentCaptor<PedidoStatusAlteradoEvent> captor = ArgumentCaptor.forClass(PedidoStatusAlteradoEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        PedidoStatusAlteradoEvent eventoPublicado = captor.getValue();
        assertEquals(88L, eventoPublicado.getPedidoId());
        assertEquals(15, eventoPublicado.getNumeroSenha());
        assertEquals(StatusPedido.RECEBIDO, eventoPublicado.getStatusAnterior());
        assertEquals(StatusPedido.EM_PREPARO, eventoPublicado.getNovoStatus());
        assertNotNull(eventoPublicado.getDataHora());
    }

    @Test
    @DisplayName("Falha em observer secundário ou no eventPublisher NÃO deve corromper a persistência do pedido")
    void falhaNoEventPublisherNaoDeveCorromperPersistenciaDoPedido() {
        NovoPedidoDTO dto = new NovoPedidoDTO();
        dto.setFormaPagamento(FormaPagamento.CARTAO_DEBITO);
        dto.setNomeCliente("Cliente Teste");

        CustomizacaoItemDTO itemDto = new CustomizacaoItemDTO();
        itemDto.setProdutoId(10L);
        itemDto.setQuantidade(1);
        dto.setItens(List.of(itemDto));

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(xSalada));
        when(pedidoRepository.findMaxNumeroSenhaByDataHoraBetween(any(), any())).thenReturn(Optional.of(99));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        // Simula falha catastrófica em um observer secundário disparado durante publishEvent
        doThrow(new RuntimeException("Falha na infraestrutura de mensageria / impressora"))
                .when(eventPublisher).publishEvent(any(PedidoCriadoEvent.class));

        PedidoResponseDTO response = assertDoesNotThrow(() -> pedidoService.processarNovoPedido(dto));

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(100, response.getNumeroSenha());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Garante que PedidoService NÃO possui referências diretas a classes visuais ou de cozinha")
    void deveGarantirDesacoplamentoEstruturalDoPedidoService() {
        Field[] fields = PedidoService.class.getDeclaredFields();
        for (Field field : fields) {
            String tipoNome = field.getType().getName();
            assertFalse(tipoNome.contains("Cozinha"), "PedidoService não deve referenciar Cozinha diretamente: " + tipoNome);
            assertFalse(tipoNome.contains("Kds"), "PedidoService não deve referenciar KDS diretamente: " + tipoNome);
            assertFalse(tipoNome.contains("Painel"), "PedidoService não deve referenciar Painel diretamente: " + tipoNome);
            assertFalse(tipoNome.contains("View"), "PedidoService não deve referenciar View diretamente: " + tipoNome);
        }
    }
}
