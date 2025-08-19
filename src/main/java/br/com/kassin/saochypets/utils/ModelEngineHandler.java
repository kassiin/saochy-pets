package br.com.kassin.saochypets.utils;

import br.com.kassin.saochypets.data.model.Pet;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.model.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Optional;

public final class ModelEngineHandler {

    private ModelEngineHandler() {
    }

    public static Optional<ModeledEntity> spawnModel(Location location, Pet pet) {
        if (location == null || location.getWorld() == null) return Optional.empty();

        String modelId = pet.getModelID();
        EntityType entityType = pet.getBaseEntity();

        LivingEntity baseEntity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
        baseEntity.setPersistent(true);
        baseEntity.setCustomNameVisible(true);
        baseEntity.setSilent(true);
        pet.setEntity(baseEntity);

        ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(pet.getModelID());
        if (blueprint == null) {
            Bukkit.getLogger().info("Modelo não encontrado: " + pet.getModelID());
            baseEntity.remove();
            return Optional.empty();
        }

        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(baseEntity);
        pet.setModeledEntity(modeledEntity);

        ActiveModel activeModel = ModelEngineAPI.createActiveModel(blueprint);
        pet.setActiveModel(activeModel);

        if (activeModel == null) {
            baseEntity.remove();
            return Optional.empty();
        }

        modeledEntity.setBaseEntityVisible(false);
        modeledEntity.addModel(activeModel, true);
        return Optional.of(modeledEntity);
    }

    public static void playAnimation(Pet pet, String animation, boolean loop) {
        ModeledEntity modeledEntity = pet.getModeledEntity();
        if (modeledEntity == null) return;

        modeledEntity.getModels().values().forEach(activeModel -> {
            activeModel.getAnimationHandler().playAnimation(animation, 0.3, 0.3, 0.3, loop);
        });
    }

    public static void destroyModel(Pet pet) {
        ModeledEntity modeledEntity = pet.getModeledEntity();
        if (modeledEntity != null) {
            modeledEntity.destroy();
        }
    }

}
