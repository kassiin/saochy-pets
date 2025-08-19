package br.com.kassin.saochypets.listeners;

import br.com.kassin.saochypets.pet.PetAccess;
import br.com.kassin.saochypets.pet.PetBehavior;
import br.com.kassin.saochypets.gui.PetGUI;
import br.com.kassin.saochypets.data.cache.PlayerActivePetCache;
import br.com.kassin.saochypets.data.model.Pet;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import br.com.kassin.saochypets.data.PetService;

public class PetGUIListener implements Listener {

    private final PetService petService;

    public PetGUIListener(PetService petService) {
        this.petService = petService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("§8§lMenu do Pet")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Pet pet = PlayerActivePetCache.getPet(player.getUniqueId()).orElse(null);
        if (pet == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();

        switch (slot) {
            case 38:
                player.closeInventory();
                player.sendMessage("§eDigite no chat o novo nome do pet:");
                RenamePetChat.waitingRename(player, pet);
                break;

            case 40:
                pet.setBehavior(cycleBehavior(pet.getBehavior()));
                petService.updatePet(pet);
                player.sendMessage("§aComportamento alterado para: " + getBehaviorFormatted(pet.getBehavior()));
                PetGUI.open(player, pet);
                break;

            //case 24:
            //    pet.setAccess(cycleAccess(pet.getAccess()));
            //    petService.updatePet(pet);
            //    player.sendMessage("§aVisibilidade alterada para: " + getAccessFormatted(pet.getAccess()));
            //    PetGUI.open(player, pet);
            //    break;

            case 53:
                player.closeInventory();
                break;
        }
    }

    private PetBehavior cycleBehavior(PetBehavior current) {
        return switch (current) {
            case PASSIVE -> PetBehavior.AGGRESSIVE;
            case AGGRESSIVE -> PetBehavior.DEFENSIVE;
            default -> PetBehavior.PASSIVE;
        };
    }

    private PetAccess cycleAccess(PetAccess current) {
        return current == PetAccess.PUBLIC ? PetAccess.PRIVATE : PetAccess.PUBLIC;
    }

    private String getBehaviorFormatted(PetBehavior behavior) {
        return switch (behavior) {
            case PASSIVE -> "§aPassivo";
            case AGGRESSIVE -> "§cAgressivo";
            case DEFENSIVE -> "§eDefensivo";
            default -> "§fDesconhecido";
        };
    }

    private String getAccessFormatted(PetAccess access) {
        return access == PetAccess.PUBLIC ? "§aPúblico" : "§7Privado";
    }
}