package br.com.kassin.saochypets.vehicle;

import br.com.kassin.saochypets.data.model.Pet;
import lombok.Getter;

@Getter
public enum PetFlyingDirection {
    NONE("Parado / Sem voo"),
    FLYING_DISABLE(""),
    FLYING_ENABLE("");

    private final String description;

    PetFlyingDirection(String description) {
        this.description = description;
    }


}

