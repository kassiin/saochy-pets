package br.com.kassin.saochypets.gui;

import br.com.kassin.saochypets.pet.PetAccess;
import br.com.kassin.saochypets.pet.PetBehavior;
import br.com.kassin.saochypets.data.model.Pet;
import br.com.kassin.saochypets.manager.LevelingManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class PetGUI {

    private static final String TITLE = "§8§lMenu do Pet";
    private static final ItemStack GLASS_PANE = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");

    public static void open(Player player, Pet pet) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        fillBorders(inv);
        setHeader(inv, pet);
        setInfoSection(inv, pet);
        setActionSection(inv, pet);

        player.openInventory(inv);
    }

    private static void fillBorders(Inventory inv) {
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, GLASS_PANE);
            }
        }
    }

    private static void setHeader(Inventory inv, Pet pet) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName("§6§l" + pet.getDisplayName());
        meta.setLore(Arrays.asList(
                "§7ID: §f" + pet.getPetId(),
                "§7Nível: §a" + pet.getLevel(),
                "§7Raridade: " + pet.getRarity().displayName(),
                ""
        ));
        head.setItemMeta(meta);

        inv.setItem(4, head);
    }

    private static void setInfoSection(Inventory inv, Pet pet) {
        ItemStack stats = createItem(Material.BOOK, "§a§lStatus do Pet", Arrays.asList(
                "§7Dano: §c" + pet.getBaseDamage(),
                "§7Velocidade: §f" + pet.getSpeed(),
                "§7Comportamento: " + getBehaviorColor(pet.getBehavior()) + pet.getBehavior(),
                "",
                "§7XP: §e" + pet.getXp() + "§7/§e" + LevelingManager.getXpNeededForLevel(pet.getLevel())
        ));
        inv.setItem(20, stats);

        ItemStack progress = createItem(Material.EXPERIENCE_BOTTLE, "§b§lProgresso", Arrays.asList(
                "§7Progresso para o próximo nível:",
                "§e" + pet.getXp() + "§7/§e" + LevelingManager.getXpNeededForLevel(pet.getLevel()),
                getProgressBar(pet.getXp(), (int) LevelingManager.getXpNeededForLevel(pet.getLevel())),
                "§7" + calculatePercentage(pet.getXp(), (int) LevelingManager.getXpNeededForLevel(pet.getLevel())) + "%"
        ));
        inv.setItem(22, progress);
    }

    private static void setActionSection(Inventory inv, Pet pet) {
        inv.setItem(38, createItem(Material.NAME_TAG, "§e§lRenomear Pet", Arrays.asList(
                "§7Altere o nome do seu pet",
                "",
                "§eClique para renomear"
        )));

        inv.setItem(40, createItem(Material.BONE, "§6§lComportamento", Arrays.asList(
                "§7Atual: " + getBehaviorColor(pet.getBehavior()) + pet.getBehavior(),
                "",
                "§7Clique para alternar"
        )));

        inv.setItem(24, createItem(Material.OAK_DOOR, "§b§lVisibilidade", Arrays.asList(
                "§7Atual: " + (pet.getAccess() == PetAccess.PUBLIC ? "§aPúblico" : "§7Privado"),
                "",
                "§7Clique para alternar"
        )));

        inv.setItem(53, createItem(Material.BARRIER, "§c§lFechar Menu"));
    }

    private static ItemStack createItem(Material material, String name) {
        return createItem(material, name, null);
    }

    private static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static String getBehaviorColor(PetBehavior behavior) {
        return switch (behavior) {
            case PASSIVE -> "§a";
            case AGGRESSIVE -> "§c";
            case DEFENSIVE -> "§e";
            default -> "§f";
        };
    }

    private static String getProgressBar(int current, int max) {
        int totalBars = 30;
        int progressBars = (int) ((double) current / max * totalBars);
        return "§a" + StringUtils.repeat("|", progressBars) +
                "§7" + StringUtils.repeat("|", totalBars - progressBars);
    }

    private static int calculatePercentage(int current, int max) {
        return (int) (((double) current / max) * 100);
    }
}
