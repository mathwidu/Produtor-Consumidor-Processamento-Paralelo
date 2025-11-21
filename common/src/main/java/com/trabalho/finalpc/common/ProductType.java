package com.trabalho.finalpc.common; // Declara o pacote desta classe (namespace do projeto)

/**
 * Aqui eu defino os DOIS tipos de itens do problema Produtor–Consumidor.
 * Eu uso um enum para garantir segurança de tipo e evitar strings soltas.
 * Regras do enunciado ligadas a estes tipos:
 * - TIPO_A: produção em 3.5s; consumo em 7.0s (exatamente o dobro).
 * - TIPO_B: produção em 7.5s; consumo em 15.0s (exatamente o dobro).
 */
public enum ProductType { // Enum = conjunto fechado de constantes nomeadas
    TIPO_A, // Tipo A (mais rápido de produzir, mais rápido de consumir)
    TIPO_B  // Tipo B (mais lento de produzir, mais lento de consumir)
}
