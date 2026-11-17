package com.restaurante.pdv.model.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.restaurante.pdv.exception.NegocioException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Serviço responsável pela geração de payloads EMV PIX (BR Code padrão Banco Central)
 * e renderização da matriz do QR Code em imagem Base64 PNG legível.
 */
@Service
public class PixEmvService {

    private static final String ID_PAYLOAD_FORMAT_INDICATOR = "00";
    private static final String ID_MERCHANT_ACCOUNT_INFORMATION = "26";
    private static final String ID_MERCHANT_ACCOUNT_INFORMATION_GUI = "00";
    private static final String ID_MERCHANT_ACCOUNT_INFORMATION_KEY = "01";
    private static final String ID_MERCHANT_CATEGORY_CODE = "52";
    private static final String ID_TRANSACTION_CURRENCY = "53";
    private static final String ID_TRANSACTION_AMOUNT = "54";
    private static final String ID_COUNTRY_CODE = "58";
    private static final String ID_MERCHANT_NAME = "59";
    private static final String ID_MERCHANT_CITY = "60";
    private static final String ID_ADDITIONAL_DATA_FIELD_TEMPLATE = "62";
    private static final String ID_ADDITIONAL_DATA_FIELD_TXID = "05";
    private static final String ID_CRC16 = "63";

    private static final String GUI_PIX = "br.gov.bcb.pix";
    private static final String MCC_DEFAULT = "0000";
    private static final String CURRENCY_BRL = "986";
    private static final String COUNTRY_BR = "BR";

    /**
     * Gera a string completa do payload EMV (Copia e Cola) compatível com qualquer banco do ecossistema PIX.
     *
     * @param chavePix chave cadastrada (e-mail, CPF, CNPJ, telefone ou chave aleatória).
     * @param nomeRecebedor nome comercial do estabelecimento (máx. 25 caracteres).
     * @param cidadeRecebedor cidade do estabelecimento (máx. 15 caracteres).
     * @param txId identificador único alfanumérico da transação (máx. 25 caracteres).
     * @param valor valor monetário da cobrança.
     * @return payload completo pronto para pagamento copia-e-cola.
     */
    public String gerarPayload(String chavePix, String nomeRecebedor, String cidadeRecebedor, String txId, BigDecimal valor) {
        if (chavePix == null || chavePix.isBlank()) {
            throw new NegocioException("A chave PIX é obrigatória para gerar o payload EMV.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O valor do PIX deve ser maior que zero.");
        }

        String nomeSanitizado = sanitizarTexto(nomeRecebedor, 25, "Antunes Lanches");
        String cidadeSanitizada = sanitizarTexto(cidadeRecebedor, 15, "Sao Paulo");
        String txIdSanitizado = sanitizarAlfanumerico(txId, 25);
        if (txIdSanitizado.isBlank()) {
            txIdSanitizado = "***";
        }

        // Subtags da Conta do Comerciante (Tag 26)
        String guiTag = formatarTag(ID_MERCHANT_ACCOUNT_INFORMATION_GUI, GUI_PIX);
        String chaveTag = formatarTag(ID_MERCHANT_ACCOUNT_INFORMATION_KEY, chavePix.trim());
        String merchantAccountInfo = formatarTag(ID_MERCHANT_ACCOUNT_INFORMATION, guiTag + chaveTag);

        // Subtag do txId (Tag 62)
        String txIdTag = formatarTag(ID_ADDITIONAL_DATA_FIELD_TXID, txIdSanitizado);
        String additionalData = formatarTag(ID_ADDITIONAL_DATA_FIELD_TEMPLATE, txIdTag);

        // Formatação do valor (duas casas decimais)
        String valorStr = valor.setScale(2, RoundingMode.HALF_UP).toPlainString();

        StringBuilder payload = new StringBuilder();
        payload.append(formatarTag(ID_PAYLOAD_FORMAT_INDICATOR, "01"));
        payload.append(merchantAccountInfo);
        payload.append(formatarTag(ID_MERCHANT_CATEGORY_CODE, MCC_DEFAULT));
        payload.append(formatarTag(ID_TRANSACTION_CURRENCY, CURRENCY_BRL));
        payload.append(formatarTag(ID_TRANSACTION_AMOUNT, valorStr));
        payload.append(formatarTag(ID_COUNTRY_CODE, COUNTRY_BR));
        payload.append(formatarTag(ID_MERCHANT_NAME, nomeSanitizado));
        payload.append(formatarTag(ID_MERCHANT_CITY, cidadeSanitizada));
        payload.append(additionalData);

        // Anexar prefixo da Tag 63 para cálculo do CRC16
        payload.append(ID_CRC16).append("04");

        String crc = calcularCrc16(payload.toString());
        payload.append(crc);

        return payload.toString();
    }

    /**
     * Renderiza o payload em uma imagem de QR Code e codifica em Base64 PNG.
     *
     * @param payload string EMV ou texto a ser codificado.
     * @param largura largura em pixels da imagem (ex: 300).
     * @param altura altura em pixels da imagem (ex: 300).
     * @return string codificada em Base64 representando a imagem PNG.
     */
    public String gerarQrCodeBase64(String payload, int largura, int altura) {
        byte[] pngBytes = gerarQrCodePngBytes(payload, largura, altura);
        return Base64.getEncoder().encodeToString(pngBytes);
    }

    /**
     * Gera os bytes brutos do arquivo PNG do QR Code utilizando a biblioteca ZXing.
     */
    public byte[] gerarQrCodePngBytes(String payload, int largura, int altura) {
        if (payload == null || payload.isBlank()) {
            throw new NegocioException("O payload não pode ser nulo ou vazio para geração do QR Code.");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

            BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, largura, altura, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new NegocioException("Falha ao renderizar a matriz de imagem do QR Code PIX: " + e.getMessage());
        }
    }

    /**
     * Formata um campo no padrão TLV (Tag-Length-Value).
     */
    private String formatarTag(String id, String valor) {
        String safeValue = valor != null ? valor : "";
        return String.format(Locale.US, "%s%02d%s", id, safeValue.length(), safeValue);
    }

    /**
     * Remove acentos e caracteres especiais conforme exigência do padrão BR Code do BCB.
     */
    private String sanitizarTexto(String texto, int tamanhoMaximo, String fallback) {
        if (texto == null || texto.isBlank()) {
            return fallback;
        }
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9 ]", "")
                .trim();
        if (normalizado.isBlank()) {
            return fallback;
        }
        return normalizado.length() > tamanhoMaximo ? normalizado.substring(0, tamanhoMaximo) : normalizado;
    }

    /**
     * Filtra caracteres mantendo apenas alfanuméricos e limitando ao tamanho máximo.
     */
    private String sanitizarAlfanumerico(String texto, int tamanhoMaximo) {
        if (texto == null) return "";
        String limpo = texto.replaceAll("[^a-zA-Z0-9]", "");
        return limpo.length() > tamanhoMaximo ? limpo.substring(0, tamanhoMaximo) : limpo;
    }

    /**
     * Algoritmo de cálculo do CRC16-CCITT (Polinômio 0x1021, valor inicial 0xFFFF).
     */
    public static String calcularCrc16(String dados) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        byte[] bytes = dados.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i)) & 1) == 1;
                boolean c15 = ((crc >> 15) & 1) == 1;
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }
}
