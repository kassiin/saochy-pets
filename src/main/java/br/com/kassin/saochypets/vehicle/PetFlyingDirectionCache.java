package br.com.kassin.saochypets.vehicle;

import br.com.kassin.saochypets.data.model.Pet;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetFlyingDirectionCache {
    @Getter
    private static final Map<UUID, Pet> petFlyingDirectionMap = new HashMap<>();

    public static Pet getPetFlyingDirection(UUID petId) {
        return petFlyingDirectionMap.get(petId);
    }

    public static void addPetFlyingDirection(UUID petId, Pet pet) {
        petFlyingDirectionMap.put(petId, pet);
    }

    public static void removePetFlyingDirection(UUID petId) {
        petFlyingDirectionMap.remove(petId);
    }

    public static void clear() {
        petFlyingDirectionMap.clear();
    }

}
