package br.com.kassin.saochypets.manager;

import br.com.kassin.saochypets.pet.PetRarity;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Getter
public class LevelingManager {

    private final static Map<String, PetRarity> rarities = new LinkedHashMap<>();
    private final static Map<EntityType, Integer> xpValues = new EnumMap<>(EntityType.class);

    private static double baseXp = 100;
    private static double xpMultiplier = 1.5;
    @Getter
    private static double damageIncreasePerLevel = 1.2;
    private static int defaultXp = 10;

    public static void load(FileConfiguration config, Logger logger) {
        ConfigurationSection levelSection = config.getConfigurationSection("level_system");
        if (levelSection != null) {
            baseXp = levelSection.getDouble("base_xp", 100);
            xpMultiplier = levelSection.getDouble("xp_multiplier", 1.5);
            damageIncreasePerLevel = levelSection.getDouble("damage_increase_per_level", 1.2);
        }

        rarities.clear();
        ConfigurationSection raritySection = config.getConfigurationSection("rarities");
        if (raritySection != null) {
            for (String key : raritySection.getKeys(false)) {
                String name = raritySection.getString(key + ".name", key);

                double damageMultiplier = raritySection.getDouble(key + ".damage_multiplier", 1.0);

                double rarityBaseXp = raritySection.getDouble(key + ".base_xp", baseXp);
                double rarityXpMultiplier = raritySection.getDouble(key + ".xp_multiplier", xpMultiplier);
                double rarityDamagePerLevel = raritySection.getDouble(key + ".damage_increase_per_level", damageIncreasePerLevel);

                PetRarity rarity = new PetRarity(
                        key.toUpperCase(),
                        name,
                        damageMultiplier,
                        rarityBaseXp,
                        rarityXpMultiplier,
                        rarityDamagePerLevel
                );

                rarities.put(key.toUpperCase(), rarity);
            }
        }
        logger.info(rarities.size() + " raridades foram carregadas!");

        xpValues.clear();
        ConfigurationSection xpSection = config.getConfigurationSection("xp_values");
        if (xpSection != null) {
            defaultXp = xpSection.getInt("default", 10);
            for (String key : xpSection.getKeys(false)) {
                if (key.equalsIgnoreCase("default")) continue;
                try {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    xpValues.put(type, xpSection.getInt(key));
                } catch (IllegalArgumentException e) {
                    logger.warning("Tipo de entidade inválido no levels_and_rarities.yml: " + key);
                }
            }
        }
        logger.info(defaultXp + " valor de XP padrão foi carregado!");
        logger.info(xpValues.size() + " valores de XP por entidade foram carregados!");
    }

    public static int getXpForEntity(EntityType entityType) {
        return xpValues.getOrDefault(entityType, defaultXp);
    }

    public static PetRarity getRarityById(String id) {
        return rarities.get(id.toUpperCase());
    }

    public static double getXpNeededForLevel(int level) {
        if (level <= 1) return baseXp;
        return baseXp * (level * xpMultiplier);
    }
}