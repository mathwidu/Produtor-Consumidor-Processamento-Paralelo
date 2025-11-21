package com.trabalho.finalpc.consumer; // Pacote do consumidor (onde está a interface NeedSelectionStrategy)

import com.trabalho.finalpc.common.ProductType;           // Enum com os tipos de produto (TIPO_A, TIPO_B)
import java.util.concurrent.ThreadLocalRandom;            // Gerador aleatório eficiente e thread-safe

/**
 * Eu optei por uma seleção aleatória uniforme da necessidade.
 * Isso atende ao enunciado e simplifica a apresentação.
 */
public class RandomNeedSelectionStrategy implements NeedSelectionStrategy { // Implementa a interface

    @Override // Implementação do método da interface
    public ProductType nextNeededType() { // Retorna o próximo tipo necessário
        boolean pickA = ThreadLocalRandom.current().nextBoolean(); // true ~50%, false ~50%
        return pickA ? ProductType.TIPO_A : ProductType.TIPO_B;    // Converte boolean em TIPO_A/TIPO_B
    }
}
