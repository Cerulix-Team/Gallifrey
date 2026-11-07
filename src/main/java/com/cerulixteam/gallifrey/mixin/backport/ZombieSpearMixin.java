package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.BackportCombat;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mounts of Mayhem (1.21.11): some zombies spawn holding a spear, and a few ride zombie horses
 * (Zombie Horsemen). Only plain adult zombies in the Overworld, not husks or drowned.
 */
@Mixin(ZombieEntity.class)
public abstract class ZombieSpearMixin {
    @Inject(method = "initialize", at = @At("TAIL"))
    private void gallifrey$spears(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason reason, EntityData data, NbtCompound nbt,
                                  CallbackInfoReturnable<EntityData> cir) {
        ZombieEntity self = (ZombieEntity) (Object) this;
        if (self.getType() != EntityType.ZOMBIE || self.isBaby() || reason != SpawnReason.NATURAL || self.hasVehicle()) return;
        if (!self.getMainHandStack().isEmpty() || self.getRandom().nextFloat() > 0.06F * difficulty.getClampedLocalDifficulty() + 0.02F) return;
        self.equipStack(EquipmentSlot.MAINHAND, new ItemStack(self.getRandom().nextInt(3) == 0 ? BackportCombat.IRON_SPEAR : BackportCombat.STONE_SPEAR));
        if (self.getRandom().nextInt(4) == 0) {
            ZombieHorseEntity horse = EntityType.ZOMBIE_HORSE.create(self.getWorld());
            if (horse != null) {
                horse.refreshPositionAndAngles(self.getX(), self.getY(), self.getZ(), self.getYaw(), 0.0F);
                horse.setTame(true);
                horse.saddle(null);
                horse.setPersistent();
                world.spawnEntity(horse);
                self.startRiding(horse);
            }
        }
    }
}
