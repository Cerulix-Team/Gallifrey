package com.cerulixteam.gallifrey.block.entity;

import com.cerulixteam.gallifrey.block.GallifreyModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Persistent state for the physical TARDIS console. */
public class TardisConsoleBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private boolean powered = true;

    public TardisConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(GallifreyModBlockEntities.TARDIS_CONSOLE, pos, state);
    }

    public boolean isPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        markDirty();
        if (world != null && !world.isClient) world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static Hartnell console model. Controls are handled by the TARDIS GUI.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public double getTick(Object object) {
        return world == null ? 0.0D : world.getTime();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putBoolean("Powered", powered);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        powered = !nbt.contains("Powered") || nbt.getBoolean("Powered");
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
}
