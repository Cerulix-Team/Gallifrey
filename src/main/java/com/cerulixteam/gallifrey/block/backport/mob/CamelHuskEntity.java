package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Camel Husk (1.21.11): an undead camel that turns up in deserts at night, ridden by a Husk with a Parched
 * behind it. It can be ridden once its riders are dealt with.
 */
public class CamelHuskEntity extends CamelEntity {
    public CamelHuskEntity(EntityType<? extends CamelEntity> type, World world) {
        super(type, world);
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;
    }

    @Nullable
    @Override
    public CamelEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean canBreedWith(net.minecraft.entity.passive.AnimalEntity other) {
        return false;
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason reason, @Nullable EntityData data, @Nullable NbtCompound nbt) {
        EntityData result = super.initialize(world, difficulty, reason, data, nbt);
        if (reason == SpawnReason.NATURAL || reason == SpawnReason.SPAWN_EGG) {
            HuskEntity husk = EntityType.HUSK.create(world.toServerWorld());
            BackportSkeletonEntity parched = BackportMobs.PARCHED.create(world.toServerWorld());
            if (husk != null) {
                husk.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0);
                husk.initialize(world, difficulty, SpawnReason.JOCKEY, null, null);
                husk.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, new net.minecraft.item.ItemStack(com.cerulixteam.gallifrey.block.backport.BackportCombat.IRON_SPEAR));
                world.spawnEntity(husk);
                husk.startRiding(this);
            }
            if (parched != null) {
                parched.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0);
                parched.initialize(world, difficulty, SpawnReason.JOCKEY, null, null);
                world.spawnEntity(parched);
                parched.startRiding(this);
            }
        }
        return result;
    }
}
