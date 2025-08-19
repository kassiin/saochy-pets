package br.com.kassin.saochypets.data;

import br.com.kassin.saochypets.data.cache.PetCache;
import br.com.kassin.saochypets.data.cache.PlayerActivePetCache;
import br.com.kassin.saochypets.data.model.Pet;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class PetService {

    private final PlayerPetDataProvider dataProvider;

    public void updatePet(Pet pet) {
        dataProvider.updatePet(pet.getOwner().getUniqueId(), pet);
    }

    public void activatePet(Player player, String petId) {
        if (PlayerActivePetCache.getPet(player.getUniqueId()).isPresent()) {
            player.sendMessage("§cVocê já tem um pet ativo.");
            return;
        }

        Optional<Pet> petToSpawn = dataProvider.getPet(player.getUniqueId(), petId);
        Pet template = PetCache.getPet(petId);

        if (petToSpawn.isEmpty()) {
            player.sendMessage("§cVocê não possui um pet com o ID '" + petId + "'.");
            return;
        }

        Pet petData = petToSpawn.get();
        petData.spawn(player);
        petData.setFlyAnimation(template.getFlyAnimation());
        player.sendMessage("§aSeu pet " + petData.getDisplayName() + " foi ativado!");
    }

    public void deactivatePet(Player player) {
        Optional<Pet> activePetOpt = PlayerActivePetCache.removeActivePet(player.getUniqueId());

        if (activePetOpt.isEmpty()) {
            player.sendMessage("§cVocê não tem um pet ativo para remover.");
            return;
        }

        Pet pet = activePetOpt.get();

        pet.removeDriver(player);
        pet.destroy();
        player.sendMessage("§aSeu pet " + pet.getDisplayName() + " foi guardado.");
    }

    public Pet getActivePet(Player player) {
        return PlayerActivePetCache.getPet(player.getUniqueId()).orElse(null);
    }
}
