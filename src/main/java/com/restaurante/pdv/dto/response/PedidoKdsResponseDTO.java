package com.restaurante.pdv.dto.response;

import com.restaurante.pdv.model.entity.FormaPagamento;
import com.restaurante.pdv.model.entity.Pedido;
import com.restaurante.pdv.model.entity.StatusPedido;
import com.restaurante.pdv.model.service.SenhaSequencialService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO de resposta para o display operacional da cozinha (KDS - Kitchen Display System).
 * Contém tempo de espera em minutos para controle de SLA, indicação de cores (verde, amarelo, vermelho),
 * próximo status na máquina de estados e lista de itens formatados para produção.
 */
public class PedidoKdsResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer numeroSenha;
    private String senhaFormatada;
    private LocalDateTime dataHora;
    private StatusPedido status;
    private StatusPedido proximoStatus;
    private Long tempoEsperaMinutos;
    private String alertaSla;
    private String slaDescricao;
    private String nomeCliente;
    private String cpfCliente;
    private Boolean pago = Boolean.FALSE;
    private FormaPagamento formaPagamento;
    private BigDecimal valorTotal;
    private Integer quantidadeItens;
    private List<ItemKdsResponseDTO> itens = new ArrayList<>();

    public PedidoKdsResponseDTO() {
    }

    public PedidoKdsResponseDTO(Long id, Integer numeroSenha, LocalDateTime dataHora, StatusPedido status,
                                StatusPedido proximoStatus, Long tempoEsperaMinutos, String alertaSla,
                                String slaDescricao, String nomeCliente, String cpfCliente, Boolean pago,
                                FormaPagamento formaPagamento, BigDecimal valorTotal,
                                Integer quantidadeItens, List<ItemKdsResponseDTO> itens) {
        this.id = id;
        this.numeroSenha = numeroSenha;
        this.senhaFormatada = SenhaSequencialService.formatarSenha(numeroSenha);
        this.dataHora = dataHora;
        this.status = status;
        this.proximoStatus = proximoStatus;
        this.tempoEsperaMinutos = tempoEsperaMinutos;
        this.alertaSla = alertaSla;
        this.slaDescricao = slaDescricao;
        this.nomeCliente = nomeCliente;
        this.cpfCliente = cpfCliente;
        this.pago = pago;
        this.formaPagamento = formaPagamento;
        this.valorTotal = valorTotal;
        this.quantidadeItens = quantidadeItens;
        this.itens = itens != null ? itens : new ArrayList<>();
    }

    /**
     * Mapeia a entidade Pedido para o DTO do KDS, calculando tempo de espera e alerta de SLA em tempo real.
     */
    public static PedidoKdsResponseDTO deEntidade(Pedido pedido) {
        return deEntidade(pedido, LocalDateTime.now());
    }

    /**
     * Mapeia a entidade Pedido para o DTO do KDS com base em um instante de referência (útil para testes).
     */
    public static PedidoKdsResponseDTO deEntidade(Pedido pedido, LocalDateTime agora) {
        if (pedido == null) return null;

        LocalDateTime horaReferencia = agora != null ? agora : LocalDateTime.now();
        long minutosEspera = 0L;
        if (pedido.getDataHora() != null) {
            minutosEspera = Math.max(0L, Duration.between(pedido.getDataHora(), horaReferencia).toMinutes());
        }

        String alertaSla;
        String slaDescricao;
        if (minutosEspera < 5) {
            alertaSla = "VERDE";
            slaDescricao = "No prazo (< 5 min)";
        } else if (minutosEspera <= 10) {
            alertaSla = "AMARELO";
            slaDescricao = "Atenção (5 a 10 min)";
        } else {
            alertaSla = "VERMELHO";
            slaDescricao = "Atrasado (> 10 min)";
        }

        StatusPedido statusAtual = pedido.getStatus();
        StatusPedido proximo = statusAtual != null ? statusAtual.proximoStatus() : null;

        List<ItemKdsResponseDTO> itensKds = pedido.getItens() != null
                ? pedido.getItens().stream().map(ItemKdsResponseDTO::deEntidade).collect(Collectors.toList())
                : new ArrayList<>();

        int totalItens = itensKds.stream()
                .mapToInt(ItemKdsResponseDTO::getQuantidade)
                .sum();

        return new PedidoKdsResponseDTO(
                pedido.getId(),
                pedido.getNumeroSenha(),
                pedido.getDataHora(),
                statusAtual,
                proximo,
                minutosEspera,
                alertaSla,
                slaDescricao,
                pedido.getNomeCliente(),
                pedido.getCpfCliente(),
                pedido.isPago(),
                pedido.getFormaPagamento(),
                pedido.getValorTotal(),
                totalItens,
                itensKds
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroSenha() {
        return numeroSenha;
    }

    public void setNumeroSenha(Integer numeroSenha) {
        this.numeroSenha = numeroSenha;
    }

    public String getSenhaFormatada() {
        return senhaFormatada;
    }

    public void setSenhaFormatada(String senhaFormatada) {
        this.senhaFormatada = senhaFormatada;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public StatusPedido getProximoStatus() {
        return proximoStatus;
    }

    public void setProximoStatus(StatusPedido proximoStatus) {
        this.proximoStatus = proximoStatus;
    }

    public Long getTempoEsperaMinutos() {
        return tempoEsperaMinutos;
    }

    public void setTempoEsperaMinutos(Long tempoEsperaMinutos) {
        this.tempoEsperaMinutos = tempoEsperaMinutos;
    }

    public String getAlertaSla() {
        return alertaSla;
    }

    public void setAlertaSla(String alertaSla) {
        this.alertaSla = alertaSla;
    }

    public String getSlaDescricao() {
        return slaDescricao;
    }

    public void setSlaDescricao(String slaDescricao) {
        this.slaDescricao = slaDescricao;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }

    public void setCpfCliente(String cpfCliente) {
        this.cpfCliente = cpfCliente;
    }

    public Boolean getPago() {
        return pago;
    }

    public void setPago(Boolean pago) {
        this.pago = pago;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Integer getQuantidadeItens() {
        return quantidadeItens;
    }

    public void setQuantidadeItens(Integer quantidadeItens) {
        this.quantidadeItens = quantidadeItens;
    }

    public List<ItemKdsResponseDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemKdsResponseDTO> itens) {
        this.itens = itens;
    }
}
