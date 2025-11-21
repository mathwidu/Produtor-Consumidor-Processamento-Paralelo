package com.trabalho.finalpc.common.serialization; // Pacote de (de)serialização

import com.fasterxml.jackson.databind.ObjectMapper;           // Jackson para JSON
import com.trabalho.finalpc.common.ProductMessage;            // Tipo que vamos (de)serializar

/**
 * Nesta classe eu isolo a lógica de JSON (Jackson) do restante do código.
 * Eu optei por manter um ObjectMapper estático, reutilizável e thread-safe (uso padrão do Jackson).
 */
public class JacksonProductMessageCodec implements JsonCodec<ProductMessage> {
    private static final ObjectMapper MAPPER = new ObjectMapper(); // Mapper padrão é suficiente para nosso DTO simples

    @Override
    public String toJson(ProductMessage value) throws Exception { // Converte DTO -> JSON string
        return MAPPER.writeValueAsString(value); // Jackson lida com enums, strings e números automaticamente
    }

    @Override
    public ProductMessage fromJson(String json) throws Exception { // Converte JSON string -> DTO
        return MAPPER.readValue(json, ProductMessage.class); // Decisão: contrato simples, sem validadores extras
    }
}
