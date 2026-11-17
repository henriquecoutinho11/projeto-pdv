package com.restaurante.pdv.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.model.service.PixEmvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - PixEmvService")
class PixEmvServiceTest {

    private PixEmvService pixEmvService;

    @BeforeEach
    void setUp() {
        pixEmvService = new PixEmvService();
    }

    @Test
    @DisplayName("Deve gerar payload EMV no formato padrão Banco Central (BR Code)")
    void deveGerarPayloadEmvValido() {
        String chavePix = "financeiro@antuneslanches.com.br";
        String nome = "Antunes Lanches";
        String cidade = "Sao Paulo";
        String txId = "AL1001TESTE";
        BigDecimal valor = new BigDecimal("32.50");

        String payload = pixEmvService.gerarPayload(chavePix, nome, cidade, txId, valor);

        assertNotNull(payload);
        assertTrue(payload.startsWith("000201"), "Payload deve iniciar com format indicator 000201");
        assertTrue(payload.contains("br.gov.bcb.pix"), "Payload deve conter GUI do PIX");
        assertTrue(payload.contains(chavePix), "Payload deve conter chave PIX informada");
        assertTrue(payload.contains("52040000"), "Payload deve conter MCC padrão 0000");
        assertTrue(payload.contains("5303986"), "Payload deve conter moeda Real (986)");
        assertTrue(payload.contains("540532.50"), "Payload deve conter valor formatado em duas casas decimais");
        assertTrue(payload.contains("5802BR"), "Payload deve conter código país BR");
        assertTrue(payload.contains("6304"), "Payload deve conter tag 63 do CRC16");
        assertEquals(4, payload.substring(payload.indexOf("6304") + 4).length(), "CRC16 deve ter 4 caracteres hexadecimais");
    }

    @Test
    @DisplayName("Deve calcular CRC16-CCITT com exatidão")
    void deveCalcularCrc16ComExatidao() {
        String dadosExemplo = "00020126580014br.gov.bcb.pix0136123e4567-e89b-12d3-a456-426614174000520400005303986540510.005802BR5913Fulano de Tal6008BRASILIA62070503***6304";
        String crc = PixEmvService.calcularCrc16(dadosExemplo);

        assertNotNull(crc);
        assertEquals(4, crc.length());
    }

    @Test
    @DisplayName("Deve rejeitar valor nulo ou menor ou igual a zero")
    void deveRejeitarValorInvalido() {
        assertThrows(NegocioException.class, () ->
                pixEmvService.gerarPayload("pix@teste.com", "Antunes", "Sao Paulo", "TX1", BigDecimal.ZERO)
        );

        assertThrows(NegocioException.class, () ->
                pixEmvService.gerarPayload("pix@teste.com", "Antunes", "Sao Paulo", "TX1", new BigDecimal("-10.00"))
        );

        assertThrows(NegocioException.class, () ->
                pixEmvService.gerarPayload("pix@teste.com", "Antunes", "Sao Paulo", "TX1", null)
        );
    }

    @Test
    @DisplayName("Deve rejeitar chave PIX nula ou vazia")
    void deveRejeitarChavePixInvalida() {
        assertThrows(NegocioException.class, () ->
                pixEmvService.gerarPayload("", "Antunes", "Sao Paulo", "TX1", new BigDecimal("10.00"))
        );

        assertThrows(NegocioException.class, () ->
                pixEmvService.gerarPayload(null, "Antunes", "Sao Paulo", "TX1", new BigDecimal("10.00"))
        );
    }

    @Test
    @DisplayName("Deve gerar imagem QR Code Base64 legível e decodificável de volta para o payload original")
    void deveGerarQrCodeBase64ELerDeVolta() throws Exception {
        String chavePix = "pix@antuneslanches.com.br";
        BigDecimal valor = new BigDecimal("25.00");
        String payload = pixEmvService.gerarPayload(chavePix, "Antunes Lanches", "Sao Paulo", "TX123", valor);

        String base64QrCode = pixEmvService.gerarQrCodeBase64(payload, 300, 300);

        assertNotNull(base64QrCode);
        assertFalse(base64QrCode.isBlank());

        // Decodificar Base64 para BufferedImage e ler de volta com ZXing QRCodeReader
        byte[] imageBytes = Base64.getDecoder().decode(base64QrCode);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        assertNotNull(bufferedImage);

        BufferedImageLuminanceSource luminanceSource = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));

        QRCodeReader reader = new QRCodeReader();
        com.google.zxing.Result result = reader.decode(binaryBitmap);

        assertEquals(payload, result.getText(), "O conteúdo do QR Code gerado deve coincidir exatamente com o payload EMV");
    }
}
