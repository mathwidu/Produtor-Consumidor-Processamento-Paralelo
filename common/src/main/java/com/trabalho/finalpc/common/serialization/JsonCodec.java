package com.trabalho.finalpc.common.serialization; // Pacote de (de)serialização

/**
 * Eu defini uma pequena abstração para trocar facilmente a lib de JSON, se preciso.
 * Implementação concreta neste projeto: Jackson (ObjectMapper).
 */
public interface JsonCodec<T> {
    /** Serializa um objeto para JSON (String). */
    String toJson(T value) throws Exception;

    /** Desserializa um JSON (String) para objeto. */
    T fromJson(String json) throws Exception;
}
