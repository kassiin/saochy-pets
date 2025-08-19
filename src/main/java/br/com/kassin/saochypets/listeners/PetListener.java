package br.com.kassin.saochypets.listeners;

import br.com.kassin.saochypets.pet.PetBehavior;
import br.com.kassin.saochypets.utils.PetPersistentDataContainer;
import br.com.kassin.saochypets.data.PetService;
import br.com.kassin.saochypets.data.model.Pet;
import br.com.kassin.saochypets.vehicle.PetFlyingDirection;
import br.com.kassin.saochypets.vehicle.PetFlyingDirectionCache;
import br.com.kassin.saochypets.vehicle.cache.VehicleCache;
import br.com.kassin.saochypets.data.cache.PlayerActivePetCache;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PetListener implements Listener {

    private final PetService petService;

    public PetListener(PetService petService) {
        this.petService = petService;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (!player.isSneaking()) return;

        Entity entity = VehicleCache.getVehicleOwner(player.getUniqueId());

        if (entity == null) return;

        PlayerActivePetCache.getPet(player.getUniqueId()).ifPresent(pet -> {
            if (pet.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                pet.getEntity().setGravity(true);
                pet.removeDriver(player);
                player.sendMessage("§aVocê desceu do pet!");
            }
        });
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (target == null) return;
        if (PlayerActivePetCache.getAllActivePetsIds().contains(target.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onServerStop(PluginDisableEvent event) {
        try {
            PlayerActivePetCache.getAllActivePets().forEach(Pet::destroy);
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onDamageTarget(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof LivingEntity target)) return;

        Entity damager = event.getDamager();

        if (!(damager instanceof Player player)) return;

        PlayerActivePetCache.getPet(player.getUniqueId()).ifPresent(pet -> {
//            if (event.getEntity().getUniqueId().equals(pet.getEntity().getUniqueId())) {
//                event.setCancelled(true);
//                return;
//            }
            if (pet.getBehavior() != PetBehavior.AGGRESSIVE) return;
            pet.setTarget(target);
        });
    }

    @EventHandler
    public void onPetDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        PlayerActivePetCache.getPetById(entity.getUniqueId()).ifPresent(pet -> event.setCancelled(true));
    }

    @EventHandler
    public void onMountable(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (!(entity instanceof LivingEntity livingEntity)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        PlayerActivePetCache.getPet(player.getUniqueId()).ifPresent(pet -> {
            if (VehicleCache.getVehicleOwner(player.getUniqueId()) != null) return;

            if (pet.getEntity().getUniqueId().equals(livingEntity.getUniqueId())) {
                boolean mountable = PetPersistentDataContainer.isMountable(livingEntity);
                if (mountable) {
                    pet.setDriver(player);
                    player.sendMessage("§aVocê montou em um pet!");
                }
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onOwnerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player owner)) return;

        PlayerActivePetCache.getPet(owner.getUniqueId()).ifPresent(pet -> {
            if (pet.getBehavior() == PetBehavior.DEFENSIVE) {
                Entity damager = event.getDamager();
                if (damager instanceof LivingEntity livingDamager) {
                    pet.setTarget(livingDamager);
                    owner.sendMessage("§cSeu pet entrou em combate para defender você!");
                }
            }
        });
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            PlayerActivePetCache.getPetById(event.getEntity().getUniqueId()).ifPresent(Pet::destroy);
            return;
        }
        PlayerActivePetCache.getPet(player.getUniqueId()).ifPresent(Pet::destroy);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        petService.deactivatePet(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (VehicleCache.getVehicleOwner(player.getUniqueId()) == null) return;

        PlayerActivePetCache.getPet(player.getUniqueId()).ifPresent(pet -> {
            if (pet.canFly()) {
                PetFlyingDirection direction = pet.getFlyingDirection();

                if ((int) event.getFrom().getX() == (int) event.getTo().getX()
                        && (int) event.getFrom().getY() == (int) event.getTo().getY()
                        && (int) event.getFrom().getZ() == (int) event.getTo().getZ()) {
                    return;
                }

                Entity petEntity = pet.getEntity();

                if (direction == PetFlyingDirection.NONE) {
                    petEntity.setGravity(true);
                }
                if (direction == PetFlyingDirection.FLYING_ENABLE) {
                    Vector lookDir = player.getLocation().getDirection().normalize();
                    double moveSpeed = 0.8;
                    Vector velocity = lookDir.clone().multiply(moveSpeed);
                    petEntity.setVelocity(velocity);
                }
                if (direction == PetFlyingDirection.FLYING_DISABLE) {
                    if (pet.getLockedY() == null) {
                        pet.lockY(petEntity.getLocation().getY());
                    }

                    double targetY = pet.getLockedY();

                    Vector velocity = player.getLocation().getDirection().clone().normalize();
                    velocity.setY(0);
                    double moveSpeed = 0.5;
                    velocity.multiply(moveSpeed);

                    double currentY = petEntity.getLocation().getY();
                    double yDiff = targetY - currentY;
                    velocity.setY(yDiff);

                    petEntity.setVelocity(velocity);
                }
                if (direction != PetFlyingDirection.FLYING_DISABLE && pet.getLockedY() != null) {
                    pet.unlockY();
                }
            }
        });
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

        if (itemInMainHand.getType() != Material.AIR) return;

        Entity vehicle = VehicleCache.getVehicleOwner(player.getUniqueId());

        if (vehicle == null) return;

        PlayerActivePetCache.getPet(player.getUniqueId()).ifPresent(pet -> {
            if (!pet.getEntity().getUniqueId().equals(vehicle.getUniqueId())) return;
            if (!pet.canFly()) return;

            Location loc = pet.getEntity().getLocation();
            int y = loc.getBlockY() - 1;
            while (y > 0 && loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
                y--;
            }
            double distanceToGround = loc.getY() - y - 1;

            PetFlyingDirection direction = pet.getFlyingDirection();
            if (distanceToGround <= 5) {
                if (direction == PetFlyingDirection.FLYING_ENABLE) {
                    pet.setFlyingDirection(PetFlyingDirection.FLYING_DISABLE);
                    pet.getEntity().setGravity(false);
                }
                if (direction == PetFlyingDirection.FLYING_DISABLE) {
                    pet.setFlyingDirection(PetFlyingDirection.NONE);
                    pet.getEntity().setGravity(true);
                }
                if (direction == PetFlyingDirection.NONE) {
                    pet.setFlyingDirection(PetFlyingDirection.FLYING_ENABLE);
                }
            } else {
                if (direction == PetFlyingDirection.FLYING_ENABLE) {
                    pet.setFlyingDirection(PetFlyingDirection.FLYING_DISABLE);
                } else {
                    pet.setFlyingDirection(PetFlyingDirection.FLYING_ENABLE);
                }
                pet.getEntity().setGravity(false);
            }

            PetFlyingDirectionCache.addPetFlyingDirection(player.getUniqueId(), pet);

        });
    }
}
