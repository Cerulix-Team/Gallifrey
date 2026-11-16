package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A Cushion (26.3): something to sit on. It sits in the middle of the block it was put on, has no
 * collision, can't be pushed by pistons, and drops when the block under it goes. Hit it to pick it up.
 */
public class CushionEntity extends Entity {
    private static final TrackedData<Integer> COLOR = DataTracker.registerData(CushionEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public CushionEntity(EntityType<? extends CushionEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public DyeColor getColor() {
        return DyeColor.byId(this.dataTracker.get(COLOR));
    }

    public void setColor(DyeColor color) {
        this.dataTracker.set(COLOR, color.getId());
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(COLOR, DyeColor.WHITE.getId());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.setColor(DyeColor.byName(nbt.getString("Color"), DyeColor.WHITE));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("Color", this.getColor().getName());
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.getWorld();
        if (!world.isClient && this.age % 10 == 0) {
            BlockPos support = BlockPos.ofFloored(this.getX(), this.getY() - 0.01, this.getZ());
            if (world.getBlockState(support).getCollisionShape(world, support).isEmpty()) {
                this.breakAndDrop(null);
            }
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.shouldCancelInteraction() || this.hasPassengers()) return ActionResult.PASS;
        if (!this.getWorld().isClient) player.startRiding(this);
        return ActionResult.success(this.getWorld().isClient);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || this.isRemoved()) return false;
        if (!this.getWorld().isClient) {
            this.breakAndDrop(source.getAttacker() instanceof PlayerEntity player ? player : null);
        }
        return true;
    }

    private void breakAndDrop(PlayerEntity breaker) {
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (breaker == null || !breaker.getAbilities().creativeMode) {
            this.dropStack(this.getPickBlockStack());
        }
        this.discard();
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Backport26.CUSHIONS[this.getColor().getId()]);
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.25;
    }
}
