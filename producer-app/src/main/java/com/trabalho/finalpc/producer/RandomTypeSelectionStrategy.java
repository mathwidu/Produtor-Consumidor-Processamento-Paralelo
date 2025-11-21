package com.trabalho.finalpc.producer; // Pacote do produtor (onde está a interface TypeSelectionStrategy)

import com.trabalho.finalpc.common.ProductType;           // Enum com os tipos de produto (TIPO_A, TIPO_B)
import java.util.concurrent.ThreadLocalRandom;            // Random rápido e thread-safe para escolha do tipo

/**
 * Eu optei por uma estratégia simples: A ou B com 50%/50% de chance.
 * - Uso ThreadLocalRandom para evitar contenção se houver múltiplas threads.
 * - A simplicidade favorece a demonstração do enunciado.
 */
public class RandomTypeSelectionStrategy implements TypeSelectionStrategy { // Implementa a interface de estratégia

    @Override // Indica que estamos implementando o método definido na interface
    public ProductType nextType() { // Retorna o próximo tipo a produzir
        boolean pickA = ThreadLocalRandom.current().nextBoolean(); // true ~50%, false ~50%
        return pickA ? ProductType.TIPO_A : ProductType.TIPO_B;    // Mapeia boolean -> enum de forma direta
    }
}
