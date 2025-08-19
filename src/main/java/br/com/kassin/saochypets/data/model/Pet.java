package br.com.kassin.saochypets.data.model;

import br.com.kassin.saochypets.manager.LevelingManager;
import br.com.kassin.saochypets.pet.PetAccess;
import br.com.kassin.saochypets.pet.PetBehavior;
import br.com.kassin.saochypets.pet.PetRarity;
import br.com.kassin.saochypets.utils.ModelEngineHandler;
import br.com.kassin.saochypets.utils.PathfinderGoalFollowOwner;
import br.com.kassin.saochypets.utils.PetPersistentDataContainer;
import br.com.kassin.saochypets.vehicle.PetFlyingDirection;
import br.com.kassin.saochypets.vehicle.PetFlyingDirectionCache;
import br.com.kassin.saochypets.vehicle.cache.VehicleCache;
import br.com.kassin.saochypets.data.ActivePet;
import br.com.kassin.saochypets.data.cache.PlayerActivePetCache;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.mount.MountManager;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.mount.controller.walking.WalkingMountForcedController;
import lombok.Builder;
import lombok.Data;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Objects;

@Data
@Builder
public class Pet {

    private final String petId;
    private String displayName;
    private final EntityType baseEntity;
    private final String modelID;
    private ModeledEntity modeledEntity;
    private ActiveModel activeModel;
    private final boolean mountable;
    private final int baseDamage;
    private boolean canFly;
    private final MountController mountController = new WalkingMountForcedController();
    private LivingEntity target;
    private boolean aggressive;
    private int level;
    private int xp;
    private double attackRange;
    private PetRarity rarity;
    private Player owner;
    @Builder.Default
    private String flyAnimation = "fly";
    @Builder.Default
    private LivingEntity entity = null;
    @Builder.Default
    private PetFlyingDirection flyingDirection = PetFlyingDirection.NONE;
    @Builder.Default
    private Double lockedY = null;
    @Builder.Default
    private long delayWhenAttacking = 0;
    @Builder.Default
    private String attackAnimation = "attack";
    @Builder.Default
    private double speed = 0.1;
    @Builder.Default
    private PetBehavior behavior = PetBehavior.PASSIVE;
    @Builder.Default
    private PetAccess access = PetAccess.PRIVATE;
    @Builder.Default
    private double findTargetRange = 10.0;
    @Builder.Default
    private double minDistanceOwner = 2.0;
    @Builder.Default
    private double maxDistanceOwner = 6.0;
    @Builder.Default
    private double stopDistanceTarget = 3.0;
    private EntityInsentient nmsEntity;

    public void spawn(Player player) {
        ModelEngineHandler.spawnModel(player.getLocation(), this).ifPresent(modeledEntity -> {
            this.owner = player;
            this.entity.setCustomName(getDisplayName());
            this.entity.setCustomNameVisible(true);
            nmsEntity = (EntityInsentient) ((CraftEntity) entity).getHandle();
            EntityPlayer nmsOwner = ((CraftPlayer) player).getHandle();
            nmsEntity.goalSelector.a(1, new PathfinderGoalFollowOwner(nmsEntity, nmsOwner, speed, (float) maxDistanceOwner));

            AttributeInstance speedAttribute = this.entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            Objects.requireNonNull(speedAttribute).setBaseValue(speed);
            ActivePet activePet = new ActivePet(this, modeledEntity);
            PlayerActivePetCache.setActivePet(player.getUniqueId(), this);
            PetPersistentDataContainer.setMountable(entity, mountable);
        });
    }

    public void destroy() {
        ModelEngineHandler.destroyModel(this);
        PetFlyingDirectionCache.removePetFlyingDirection(entity.getUniqueId());
        PlayerActivePetCache.removeActivePet(owner.getUniqueId());
        this.modeledEntity = null;
        this.entity.remove();
    }

    public void setDriver(Player player) {
        if (!canBeMountedBy(player)) {
            player.sendMessage("§cVocê não tem permissão para montar este pet!");
            return;
        }
        MountManager mountManager = this.modeledEntity.getMountManager();
        try {
            for (int i = 0; i < 3; i++) {
                mountManager.setDriver(player, mountController);
            }
        } catch (IllegalStateException ignored) {
        }
        VehicleCache.putVehicleOwner(player.getUniqueId(), entity);
    }

    private boolean canBeMountedBy(Player player) {
        if (access == PetAccess.PUBLIC) return true;
        if (access == PetAccess.PRIVATE) {
            return PlayerActivePetCache.isOwner(player, this);
        }
        return false;
    }

    public Entity getDriver() {
        return this.modeledEntity.getMountManager().getDriver();
    }

    public void addXp(double amount) {
        this.xp += amount;
        if (owner != null) {
            owner.sendMessage("§bSeu pet " + getDisplayName() + " §bganhou §3" + amount + " XP§b!");
        }
        double xpToLevelUp = LevelingManager.getXpNeededForLevel(this.level);

        while (this.xp >= xpToLevelUp) {
            this.xp -= xpToLevelUp;
            this.level++;
            if (owner != null) {
                owner.sendMessage("§a§lLEVEL UP! §aSeu pet " + getDisplayName() + " §asubiu para o nível §e" + this.level + "§a!");
            }
            xpToLevelUp = LevelingManager.getXpNeededForLevel(this.level);
        }
    }

    public double getFinalDamage() {
        if (rarity == null) return baseDamage;

        double levelBonus = LevelingManager.getDamageIncreasePerLevel() + (level - 1);
        double totalBaseDamage = baseDamage + levelBonus;

        return totalBaseDamage * rarity.damageMultiplier();
    }

    public void removeDriver(Player player) {
        if (modeledEntity == null) return;
        MountManager mountManager = this.modeledEntity.getMountManager();
        mountManager.removeDriver();
        VehicleCache.removeVehicleOwner(player.getUniqueId());
        PetFlyingDirectionCache.removePetFlyingDirection(player.getUniqueId());
    }

    public boolean canFly() {
        return canFly;
    }

    public void lockY(double y) {
        this.lockedY = y;
    }

    public void unlockY() {
        this.lockedY = null;
    }


    @Override
    public String toString() {
        return "Pet{" +
                "petId='" + petId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", baseEntity=" + baseEntity +
                ", entity=" + entity +
                ", modelID='" + modelID + '\'' +
                ", modeledEntity=" + modeledEntity +
                '}';
    }
}
