package br.com.kassin.saochypets.data;

import br.com.kassin.saochypets.data.model.Pet;
import br.com.kassin.saochypets.data.repository.PlayerPetRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPetDataProvider {

    private final PlayerPetRepository repository;
    private final Map<UUID, Map<String, Pet>> cache = new ConcurrentHashMap<>();

    public PlayerPetDataProvider(PlayerPetRepository repository) {
        this.repository = repository;
    }

    public Map<String, Pet> getAllPets(UUID playerId) {
        return cache.computeIfAbsent(playerId, id ->
                repository.getPets(id).orElse(new ConcurrentHashMap<>())
        );
    }

    public Optional<Pet> getPet(UUID playerId, String petId) {
        return Optional.ofNullable(getAllPets(playerId).get(petId));
    }

    public void updatePet(UUID owner, Pet pet) {
        repository.updatePet(owner, pet);
        getAllPets(owner).put(pet.getPetId(), pet);
    }

    public void addPet(UUID playerId, Pet pet) {
        getAllPets(playerId).put(pet.getPetId(), pet);
        repository.addPet(playerId, pet);
    }

    public void removeOwnedPet(UUID playerId, String petId) {
        if (getAllPets(playerId).remove(petId) != null) {
            repository.removeOwnedPet(playerId, petId);
        }
    }

    public void invalidateCache(UUID playerId) {
        cache.remove(playerId);
    }

    public void clearCache() {
        cache.clear();
    }

}