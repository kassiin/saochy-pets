package br.com.kassin.saochypets.tasks;

import br.com.kassin.saochypets.SaochyPetsPlugin;
import br.com.kassin.saochypets.data.cache.PlayerActivePetCache;
import br.com.kassin.saochypets.data.model.Pet;
import br.com.kassin.saochypets.vehicle.PetFlyingDirection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetFlyingAnimationTask extends BukkitRunnable {

    private final Map<UUID, Integer> animationCounters = new HashMap<>();
    private final int animationIntervalTicks = 20;

    @Override
    public void run() {
        for (Pet pet : PlayerActivePetCache.getActivePetsMap().values()) {
            if (!pet.canFly()) continue;

            Player owner = pet.getOwner();
            if (owner == null || !owner.isOnline()) continue;

            PetFlyingDirection direction = pet.getFlyingDirection();
            if (direction == PetFlyingDirection.NONE) {
                animationCounters.put(owner.getUniqueId(), 0);
                continue;
            }

            int ticks = animationCounters.getOrDefault(owner.getUniqueId(), 0);
            if (ticks == 0) {

                pet.getActiveModel().getAnimationHandler()
                        .playAnimation(pet.getFlyAnimation(), 0, 0, 0.9, true);
            }
            ticks++;
            if (ticks > animationIntervalTicks) ticks = 0;
            animationCounters.put(owner.getUniqueId(), ticks);
        }
    }

    public static void startTask() {
        new PetFlyingAnimationTask().runTaskTimer(SaochyPetsPlugin.getInstance(), 1L, 2L);
    }
}

