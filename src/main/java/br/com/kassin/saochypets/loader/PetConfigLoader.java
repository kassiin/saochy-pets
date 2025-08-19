package br.com.kassin.saochypets.loader;

import br.com.kassin.saochypets.pet.PetBehavior;
import br.com.kassin.saochypets.pet.PetRarity;
import br.com.kassin.saochypets.SaochyPetsPlugin;
import br.com.kassin.saochypets.data.cache.PetCache;
import br.com.kassin.saochypets.data.model.Pet;
import br.com.kassin.saochypets.manager.LevelingManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public class PetConfigLoader {

    private final FileConfiguration fileConfiguration;

    public PetConfigLoader(SaochyPetsPlugin plugin) {
        this.fileConfiguration = plugin.getPetsConfig();
    }

    public void loadPets() {
        if (!fileConfiguration.contains("pets")) return;

        fileConfiguration.getConfigurationSection("pets").getKeys(false).forEach(key -> {
            String path = "pets." + key + ".";

            String displayName = fileConfiguration.getString(path + "name", key);
            String entityTypeName = fileConfiguration.getString(path + "baseEntity", "PIG");
            String modelID = fileConfiguration.getString(path + "modelID", key);

            int maxHealth = fileConfiguration.getInt(path + "max_health", 20);
            int level = fileConfiguration.getInt(path + "level", 1);
            int xp = fileConfiguration.getInt(path + "xp", 0);

            boolean mountable = fileConfiguration.getBoolean(path + "mountable", false);
            boolean canFly = fileConfiguration.getBoolean(path + "fly", false);
            String flyAnimation = fileConfiguration.getString(path + "fly_animation", "fly");
            double speed = fileConfiguration.getDouble(path + "speed", 0.1);

            int damage = fileConfiguration.getInt(path + "damage", 3);
            double attackRange = fileConfiguration.getDouble(path + "attack_range", 2.0);
            String attackAnimation = fileConfiguration.getString(path + "attack_animation", "attack");
            long delayWhenAttacking = fileConfiguration.getLong(path + "delay_when_attacking", 0);

            PetBehavior behavior = PetBehavior.valueOf(fileConfiguration.getString(path + "behavior", "PASSIVE").toUpperCase());

            String rarityId = fileConfiguration.getString(path + "rarity", "COMMON");
            PetRarity rarity = LevelingManager.getRarityById(rarityId);

            EntityType baseEntity;

            ConfigurationSection behaviorSection = fileConfiguration.getConfigurationSection(path + "behavior_settings");
            double findTargetRange = 10.0;
            double loseTargetRange = 11.0;
            double minDistanceOwner = 2.0;
            double maxDistanceOwner = 6.0;
            double stopDistanceTarget = 3.0;

            if (behaviorSection != null) {
                findTargetRange = behaviorSection.getDouble("find_target_range", findTargetRange);
                minDistanceOwner = behaviorSection.getDouble("min_distance_owner", minDistanceOwner);
                maxDistanceOwner = behaviorSection.getDouble("max_distance_owner", maxDistanceOwner);
                stopDistanceTarget = behaviorSection.getDouble("stop_distance_target", stopDistanceTarget);
            }

            try {
                baseEntity = EntityType.valueOf(entityTypeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Tipo de entidade inválido para pet '" + key + "': " + entityTypeName);
                return;
            }

            Pet pet = Pet.builder()
                    .petId(key)
                    .displayName(displayName)
                    .baseEntity(baseEntity)
                    .modelID(modelID)
                    .mountable(mountable)
                    .baseDamage(damage)
                    .canFly(canFly)
                    .attackAnimation(attackAnimation)
                    .delayWhenAttacking(delayWhenAttacking)
                    .speed(speed)
                    .level(level)
                    .behavior(behavior)
                    .xp(xp)
                    .attackRange(attackRange)
                    .rarity(rarity)
                    .findTargetRange(findTargetRange)
                    .minDistanceOwner(minDistanceOwner)
                    .maxDistanceOwner(maxDistanceOwner)
                    .stopDistanceTarget(stopDistanceTarget)
                    .flyAnimation(flyAnimation)
                    .build();

            PetCache.putPet(key, pet);

            Bukkit.getLogger().info("§aCarregado pet '§e" + key + "§a'");
            Bukkit.getLogger().info("§b  Nome: §f" + displayName);
            Bukkit.getLogger().info("§b  Entidade base: §f" + entityTypeName);
            Bukkit.getLogger().info("§b  Modelo: §f" + modelID);
            Bukkit.getLogger().info("§b  Vida Máx: §f" + maxHealth);
            Bukkit.getLogger().info("§b  Level: §f" + level);
            Bukkit.getLogger().info("§b  XP: §f" + xp);
            Bukkit.getLogger().info("§b  Montável: §f" + mountable);
            Bukkit.getLogger().info("§b  Pode voar: §f" + canFly);
            Bukkit.getLogger().info("§b  Velocidade: §f" + speed);
            Bukkit.getLogger().info("§b  Dano base: §f" + damage);
            Bukkit.getLogger().info("§b  Alcance ataque: §f" + attackRange);
            Bukkit.getLogger().info("§b  Animação ataque: §f" + attackAnimation);
            Bukkit.getLogger().info("§b  Animação voar: §f" + flyAnimation);
            Bukkit.getLogger().info("§b  Delay ataque: §f" + delayWhenAttacking);
            Bukkit.getLogger().info("§b  Raridade: §f" + rarity);
            Bukkit.getLogger().info("§b  Comportamento: §f" + behavior);
            Bukkit.getLogger().info("§b  Alcance busca alvo: §f" + findTargetRange);
            Bukkit.getLogger().info("§b  Distância mínima do dono: §f" + minDistanceOwner);
            Bukkit.getLogger().info("§b  Distância máxima do dono: §f" + maxDistanceOwner);
            Bukkit.getLogger().info("§b  Distância parar alvo: §f" + stopDistanceTarget);
            Bukkit.getLogger().info("");

        });
        Bukkit.getLogger().info("§a" + PetCache.getPets().size() + " pets carregados.");
    }

}
