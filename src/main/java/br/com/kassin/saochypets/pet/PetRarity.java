package br.com.kassin.saochypets.pet;

public record PetRarity(String id, String displayName, double damageMultiplier, double baseXp, double xpMultiplier,
                        double damageIncreasePerLevel) {
    public double getXpNeededForLevel(int level) {
        if (level <= 1) return baseXp;
        return baseXp * (level * xpMultiplier);
    }
}

