package br.com.kassin.saochypets.data.cache;

import br.com.kassin.saochypets.data.model.Pet;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.UUID;

public class PlayerPetCache {

    private static final Cache<UUID, Pet> PETS = Caffeine.newBuilder().build();

    public void putPet(UUID playerId, Pet pet) {
        PETS.put(playerId, pet);
    }

    public Pet getPet(UUID playerId) {
        return PETS.getIfPresent(playerId);
    }

    public void removePet(UUID playerId) {
        PETS.invalidate(playerId);
    }

    public List<Pet> getPets() {
        return PETS.asMap().values().stream().toList();
    }

}
