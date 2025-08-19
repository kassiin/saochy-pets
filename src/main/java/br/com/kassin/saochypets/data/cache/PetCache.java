package br.com.kassin.saochypets.data.cache;

import br.com.kassin.saochypets.data.model.Pet;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;

public class PetCache {

    private final static Cache<String, Pet> PETS = Caffeine.newBuilder().build();

    public static Pet getPet(String key) {
        return PETS.getIfPresent(key);
    }

    public static void putPet(String key, Pet pet) {
        PETS.put(key, pet);
    }

    public static void removePet(String key) {
        PETS.invalidate(key);
    }

    public static List<Pet> getPets() {
        return PETS.asMap().values().stream().toList();
    }

    public static void clear() {
        PETS.invalidateAll();
    }
}