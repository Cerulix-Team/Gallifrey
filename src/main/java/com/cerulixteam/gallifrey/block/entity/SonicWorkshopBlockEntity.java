package com.cerulixteam.gallifrey.block.entity;

import com.cerulixteam.gallifrey.block.GallifreyModBlockEntities;
import com.cerulixteam.gallifrey.item.custom.SonicScrewdriver;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SonicWorkshopBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final String SONIC_KEY = "InstalledSonic";

    /** Charge the installed Sonic every this many ticks (20 ticks = 1 second). */
    private static final int CHARGE_INTERVAL = 20;
    /** Power added each time. 2 per second = empty to full (100) in 50 seconds. */
    private static final int CHARGE_AMOUNT = 2;

    private ItemStack sonic = ItemStack.EMPTY;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public SonicWorkshopBlockEntity(BlockPos pos, BlockState state) {
        super(GallifreyModBlockEntities.SONIC_WORKSHOP_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static workshop model; no animation controller is required.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public double getTick(Object object) {
        return world == null ? 0.0D : world.getTime();
    }

    public ItemStack getSonic() {
        return sonic;
    }

    public boolean hasSonic() {
        return !sonic.isEmpty() && sonic.getItem() instanceof SonicScrewdriver;
    }

    public void setSonic(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SonicScrewdriver)) {
            sonic = ItemStack.EMPTY;
        } else {
            sonic = stack.copy();
            sonic.setCount(1);
        }
        sync();
    }

    public ItemStack removeSonic() {
        if (sonic.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = sonic.copy();
        sonic = ItemStack.EMPTY;
        sync();
        return removed;
    }

    public void setCasing(com.cerulixteam.gallifrey.sonic.SonicCasing casing) {
        if (!hasSonic()) {
            return;
        }
        SonicScrewdriver.setCasing(sonic, casing);
        sync();
    }

    /**
     * Compatibility name used by the workshop casing packet.
     * Keep this alias so older/newer packet implementations can both
     * address the installed Sonic without changing the underlying state.
     */
    public void setSonicCasing(com.cerulixteam.gallifrey.sonic.SonicCasing casing) {
        setCasing(casing);
    }

    private void sync() {
        markDirty();
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(pos);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        // Always write the key, even with no Sonic (as an empty compound).
        // If this NBT ends up completely empty, Minecraft skips sending the
        // update packet, so the client never learns the Sonic was removed and
        // keeps rendering it. readNbt turns the empty compound back into EMPTY.
        NbtCompound sonicNbt = new NbtCompound();
        if (!sonic.isEmpty()) {
            sonic.writeNbt(sonicNbt);
        }
        nbt.put(SONIC_KEY, sonicNbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(SONIC_KEY) && !nbt.getCompound(SONIC_KEY).isEmpty()) {
            sonic = ItemStack.fromNbt(nbt.getCompound(SONIC_KEY));
            if (!(sonic.getItem() instanceof SonicScrewdriver)) {
                sonic = ItemStack.EMPTY;
            } else {
                sonic.setCount(1);
            }
        } else {
            sonic = ItemStack.EMPTY;
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    /**
     * Server tick: slowly recharges the installed Sonic.
     * Cheap when there's nothing to do - it returns straight away unless a Sonic is
     * installed, it's a charge tick, and the Sonic isn't already full. It only syncs
     * to clients when the power actually changes (at most once a second).
     */
    public static void tick(World world, BlockPos pos, BlockState state, SonicWorkshopBlockEntity blockEntity) {
        if (world.isClient || !blockEntity.hasSonic()) {
            return;
        }
        if (world.getTime() % CHARGE_INTERVAL != 0) {
            return;
        }
        if (SonicScrewdriver.getPower(blockEntity.sonic) >= SonicScrewdriver.MAX_POWER) {
            return;
        }
        SonicScrewdriver.recharge(blockEntity.sonic, CHARGE_AMOUNT);
        blockEntity.sync();
    }
}
