package com.restaurante.pdv.event;

import com.restaurante.pdv.dto.request.CustomizacaoItemDTO;
import com.restaurante.pdv.dto.request.NovoPedidoDTO;
import com.restaurante.pdv.dto.response.PedidoResponseDTO;
import com.restaurante.pdv.event.listener.CozinhaObserverListener;
import com.restaurante.pdv.event.listener.NotificacaoComprovanteListener;
import com.restaurante.pdv.model.entity.Categoria;
import com.restaurante.pdv.model.entity.FormaPagamento;
import com.restaurante.pdv.model.entity.Produto;
import com.restaurante.pdv.model.entity.StatusPedido;
import com.restaurante.pdv.model.repository.CategoriaRepository;
import com.restaurante.pdv.model.repository.ProdutoRepository;
import com.restaurante.pdv.model.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@DisplayName("Teste de Integração do Padrão Observer com Spring Events")
class ObserverIntegrationTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private CozinhaObserverListener cozinhaObserverListener;

    @Autowired
    private NotificacaoComprovanteListener comprovanteListener;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Long produtoId;

    @BeforeEach
    void setUp() {
        cozinhaObserverListener.limparFila();
        comprovanteListener.limparComprovantes();

        Categoria categoria = categoriaRepository.save(new Categoria(null, "Lanches Observer", "Teste", true));
        Produto produto = produtoRepository.save(new Produto(
                null,
                "Lanche Observer Test",
                "Descrição",
                new BigDecimal("29.90"),
                50,
                null,
                true,
                categoria
        ));
        produtoId = produto.getId();
    }

    @Test
    @DisplayName("Deve disparar todos os observers registrados ao processar um novo pedido")
    void deveDispararObserversAoCriarPedido() {
        NovoPedidoDTO dto = new NovoPedidoDTO();
        dto.setFormaPagamento(FormaPagamento.PIX);
        dto.setNomeCliente("Observer Client");

        CustomizacaoItemDTO item = new CustomizacaoItemDTO();
        item.setProdutoId(produtoId);
        item.setQuantidade(1);
        dto.setItens(List.of(item));

        PedidoResponseDTO pedidoCriado = pedidoService.processarNovoPedido(dto);

        assertNotNull(pedidoCriado);
        assertNotNull(pedidoCriado.getId());
        assertNotNull(pedidoCriado.getNumeroSenha());

        // Verificar que CozinhaObserverListener recebeu o evento e enfileirou o pedido
        assertTrue(cozinhaObserverListener.isPedidoNaFila(pedidoCriado.getId()),
                "O pedido deve estar na fila da cozinha após criação");

        // Verificar que NotificacaoComprovanteListener gerou o recibo digital
        assertTrue(comprovanteListener.isComprovanteGerado(pedidoCriado.getId()),
                "O comprovante digital deve ser gerado após criação");

        String comprovante = comprovanteListener.getComprovante(pedidoCriado.getId());
        assertNotNull(comprovante);
        assertTrue(comprovante.contains("ANTUNES LANCHES"));
        assertTrue(comprovante.contains(String.format("Pedido: #%d", pedidoCriado.getId())));
        assertTrue(comprovante.contains(String.format("Senha: %s", pedidoCriado.getSenhaFormatada())));
    }
}
