package com.trabalho.finalpc.common; // Pacote comum (contratos e utilitários)

/**
 * Aqui eu consolido os tempos definidos no enunciado.
 * Minhas decisões:
 * - Guardar em milissegundos (fácil para Thread.sleep e medições).
 * - consumptionMillis é derivado diretamente de productionMillis (garante “dobro” sem duplicar valores).
 */
public final class Timing { // Classe utilitária (métodos estáticos)

    // Constantes em milissegundos para manter os valores centralizados e claros
    private static final long PRODUCTION_A_MS = 3500L; // 3.5 segundos para TIPO_A
    private static final long PRODUCTION_B_MS = 7500L; // 7.5 segundos para TIPO_B

    private Timing() { /* Impede instanciação */ }

    /**
     * Retorna o tempo de produção (em ms) de um item do tipo informado.
     * A: 3500ms; B: 7500ms.
     * Lança IllegalArgumentException se o tipo for nulo/desconhecido.
     */
    public static long productionMillis(ProductType type) { // Retorna tempo de produção conforme tipo
        if (type == null) {
            throw new IllegalArgumentException("type não pode ser nulo");
        }
        return switch (type) {
            case TIPO_A -> PRODUCTION_A_MS;
            case TIPO_B -> PRODUCTION_B_MS;
        };
    }

    /**
     * Retorna o tempo de consumo (em ms) do tipo informado: sempre 2x a produção.
     */
    public static long consumptionMillis(ProductType type) { // Define consumo como 2x a produção (regra do enunciado)
        // Decisão importante: derivar diretamente mantém consistência quando tempos de produção mudarem
        return productionMillis(type) * 2L;
    }
}
