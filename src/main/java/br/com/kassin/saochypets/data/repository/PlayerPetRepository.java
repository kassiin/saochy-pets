package br.com.kassin.saochypets.data.repository;

import br.com.kassin.saochypets.data.model.Pet;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PlayerPetRepository {

    Optional<Map<String, Pet>> getPets(UUID playerId);

    void addPet(UUID playerId, Pet pet);

    void removeOwnedPet(UUID playerId, String petId);

    void updatePet(UUID playerId, Pet pet);

    Optional<Pet> getPet(UUID playerId, String petId);
}