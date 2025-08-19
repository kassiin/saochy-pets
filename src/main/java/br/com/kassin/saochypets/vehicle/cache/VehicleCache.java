package br.com.kassin.saochypets.vehicle.cache;

import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VehicleCache {

    private static final Map<UUID, Entity> vehicleOwners = new HashMap<>();

    public static void putVehicleOwner(UUID playerId, Entity vehicle) {
        vehicleOwners.put(playerId, vehicle);
    }

    public static Entity getVehicleOwner(UUID playerId) {
        return vehicleOwners.get(playerId);
    }

    public static void removeVehicleOwner(UUID playerId) {
        vehicleOwners.remove(playerId);
    }

}
