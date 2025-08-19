package br.com.kassin.saochypets.exception;

import br.com.kassin.saochypets.pet.PetRarity;

public class RarityNotFoundException extends RuntimeException {
    public RarityNotFoundException(PetRarity petRarity) {
        super("Raridade não encontrada: " + petRarity.id());
    }
}
