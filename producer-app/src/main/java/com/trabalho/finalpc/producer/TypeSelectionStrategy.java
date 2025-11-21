package com.trabalho.finalpc.producer; // Pacote do produtor

import com.trabalho.finalpc.common.ProductType; // Enum dos tipos de produto (TIPO_A/TIPO_B)

/**
 * Esta interface permite variar a política de escolha do tipo.
 * Minha decisão inicial: implementar uma escolha aleatória uniforme.
 * Eu poderia trocar por uma “alternante” (A,B,A,B,...) para fins de demonstração.
 */
public interface TypeSelectionStrategy {
    /** Retorna o próximo tipo de produto a ser produzido. */
    ProductType nextType();
}
