package com.trabalho.finalpc.consumer; // Pacote do consumidor

import com.trabalho.finalpc.common.ProductType; // Enum dos tipos de produto (TIPO_A/TIPO_B)

/**
 * Esta interface define como o consumidor decide “o que precisa”.
 * Eu comecei com escolha aleatória (50/50), mas posso alternar para fins didáticos.
 */
public interface NeedSelectionStrategy {
    /** Retorna o próximo tipo necessário para consumo. */
    ProductType nextNeededType();
}
