package br.com.kassin.saochypets.listeners;

import br.com.kassin.saochypets.SaochyPetsPlugin;
import br.com.kassin.saochypets.data.PetService;
import br.com.kassin.saochypets.data.PlayerPetDataProvider;
import br.com.kassin.saochypets.data.model.Pet;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RenamePetChat implements Listener {

    private static final Map<UUID, Pet> waitingRename = new HashMap<>();
    private final PetService service;

    public RenamePetChat(PetService service) {
        this.service = service;
    }

    public static void waitingRename(Player player, Pet pet) {
        waitingRename.put(player.getUniqueId(), pet);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!waitingRename.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
        Pet pet = waitingRename.remove(player.getUniqueId());
        String newName = ChatColor.translateAlternateColorCodes('&', event.getMessage());

        new BukkitRunnable() {
            @Override
            public void run() {
                pet.setDisplayName(newName);
                service.updatePet(pet);
                player.sendMessage("§aSeu pet agora se chama: §e" + newName);
            }
        }.runTask(SaochyPetsPlugin.getInstance());
    }
}

