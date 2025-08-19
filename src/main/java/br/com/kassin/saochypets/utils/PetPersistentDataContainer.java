package br.com.kassin.saochypets.utils;

import br.com.kassin.saochypets.SaochyPetsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

public class PetPersistentDataContainer {

    public static final String PET_KEY = "pet";
    public static final String MOUNTABLE_KEY = "mountable";

    public static void setPetId(Entity entity, String value) {
        entity.getPersistentDataContainer().set(
                new NamespacedKey(SaochyPetsPlugin.getInstance(), PET_KEY),
                PersistentDataType.STRING,
                value
        );
    }

    public static String getPetId(Entity entity) {
        return entity.getPersistentDataContainer().get(
                new NamespacedKey(SaochyPetsPlugin.getInstance(), PET_KEY),
                PersistentDataType.STRING
        );
    }

    public static void setMountable(Entity entity, boolean mountable) {
        entity.getPersistentDataContainer().set(
                new NamespacedKey(SaochyPetsPlugin.getInstance(), MOUNTABLE_KEY),
                PersistentDataType.BYTE,
                (byte) (mountable ? 1 : 0)
        );
    }

    public static boolean isMountable(Entity entity) {
        Byte value = entity.getPersistentDataContainer().get(
                new NamespacedKey(SaochyPetsPlugin.getInstance(), MOUNTABLE_KEY),
                PersistentDataType.BYTE
        );
        return value != null && value == 1;
    }
}
