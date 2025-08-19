package br.com.kassin.saochypets.utils;

import net.minecraft.server.v1_16_R3.*;

public class PathfinderGoalFollowOwner extends PathfinderGoal {

    private final EntityInsentient pet;
    private final EntityLiving owner;
    private final double speed;
    private final float stopDistance;

    public PathfinderGoalFollowOwner(EntityInsentient pet, EntityLiving owner, double speed, float stopDistance) {
        this.pet = pet;
        this.owner = owner;
        this.speed = speed;
        this.stopDistance = stopDistance;
    }

    @Override
    public boolean a() {
        if (owner == null) return false;
        double dist = this.pet.h(owner);
        return dist > stopDistance * stopDistance + 0.5;
    }

    @Override
    public void e() {
        this.pet.getNavigation().a(owner.locX(), owner.locY(), owner.locZ(), speed);
    }
}
