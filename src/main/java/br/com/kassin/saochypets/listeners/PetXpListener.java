package br.com.kassin.saochypets.listeners;

import br.com.kassin.saochypets.manager.LevelingManager;
import br.com.kassin.saochypets.data.PetService;
import br.com.kassin.saochypets.data.cache.PlayerActivePetCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class PetXpListener implements Listener {

    private final PetService service;

    public PetXpListener(PetService service) {
        this.service = service;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();

        if (player == null) return;

        UUID playerId = player.getUniqueId();

        PlayerActivePetCache.getPet(player.getUniqueId()).ifPresent(pet -> {
            int xpGained = LevelingManager.getXpForEntity(event.getEntityType());
            pet.addXp(xpGained);
            service.updatePet(pet);
        });
    }

}