package br.com.kassin.saochypets;

import br.com.kassin.saochypets.data.Config;
import br.com.kassin.saochypets.tasks.PetFlyingAnimationTask;
import br.com.kassin.saochypets.tasks.PetTargetTask;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class SaochyPetsPlugin extends JavaPlugin {

    @Getter
    private static SaochyPetsPlugin instance;
    @Getter
    private Config petsConfig;
    @Getter
    private Config levelsAndRaritiesConfig;
    private SaochyPetsInitializer initializer;
    private PetTargetTask petTargetTask;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        levelsAndRaritiesConfig = new Config(this, "config.yml");
        petsConfig = new Config(this, "pets.yml");
        initializer = SaochyPetsInitializer.of(this);
        startPluginTasks();
        initializer.enable();
        getLogger().info("SaochyPets ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SaochyPets desativado.");
    }

    public void reloadConfigs() {
        levelsAndRaritiesConfig.reloadDefaultConfig();
        petsConfig.reloadDefaultConfig();
        levelsAndRaritiesConfig.reloadDefaultConfig();
        initializer.reloadConfig();
    }

    private void startPluginTasks() {
        if (petTargetTask != null && !petTargetTask.isCancelled()) {
            petTargetTask.cancel();
        }
        PetFlyingAnimationTask.startTask();
        petTargetTask = new PetTargetTask();
        petTargetTask.runTaskTimer(this, 0L, 10L);
    }

}