package br.com.kassin.saochypets.data.cache;

import br.com.kassin.saochypets.data.model.Pet;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerActivePetCache {

    private static final Map<UUID, Pet> activePets = new HashMap<>();
    private static final Map<UUID, Pet> pets = new HashMap<>();

    public static Optional<Pet> getPet(UUID playerId) {
        return Optional.ofNullable(activePets.get(playerId));
    }

    public static void setActivePet(UUID playerId, Pet pet) {
        activePets.put(playerId, pet);
        pets.put(pet.getModeledEntity().getBase().getUniqueId(), pet);
    }

    public static Optional<Pet> removeActivePet(UUID playerId) {
        Pet pet = activePets.get(playerId);
        if (pet == null) {
            return Optional.empty();
        }

        pets.remove(pet.getEntity().getUniqueId());
        return Optional.ofNullable(activePets.remove(playerId));
    }

    public static boolean isOwner(Player player, Pet pet) {
        return activePets.get(player.getUniqueId()).getEntity().getUniqueId().equals(pet.getEntity().getUniqueId());
    }

    public static Collection<Pet> getAllActivePets() {
        return activePets.values();
    }

    public static Set<UUID> getAllActivePetsIds() {
        return pets.keySet();
    }

    public static void clear() {
        pets.clear();
        activePets.clear();
    }

    public static Map<UUID, Pet> getActivePetsMap() {
        return activePets;
    }

    public static Optional<Pet> getPetById(UUID uuid) {
        return Optional.ofNullable(pets.get(uuid));
    }

}