package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.world.dimension.ModDimensions;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Nether portals inside the Classic dimension go to the Classic Nether (and back) instead of
 * the vanilla Nether. 1:1 coordinates.
 *
 * On arrival it uses an existing portal nearby, otherwise it builds one with vanilla's own
 * portal builder (which finds a safe spot and adds a platform if needed, so you never end up
 * inside solid netherrack) and then swaps the frame to Classic Obsidian.
 *
 * Works for players, mobs and dropped items.
 */
@Mixin(Entity.class)
public abstract class ClassicNetherPortalMixin {

    @Shadow protected int netherPortalTime;
    @Shadow protected BlockPos lastNetherPortalPosition;
    @Shadow protected boolean inNetherPortal;

    @Shadow public abstract World getWorld();
    @Shadow public abstract boolean hasVehicle();
    @Shadow public abstract void resetPortalCooldown();
    @Shadow public abstract int getMaxNetherPortalTime();
    @Shadow protected abstract void tickPortalCooldown();

    @Inject(method = "tickPortal", at = @At("HEAD"), cancellable = true)
    private void gallifrey$classicNetherPortal(CallbackInfo ci) {
        if (!(this.getWorld() instanceof ServerWorld source)) return;
        if (!gallifrey$isClassic(source.getRegistryKey())) return;
        if (!this.inNetherPortal) return; // not in a portal: let vanilla count the timer down

        ci.cancel();
        this.inNetherPortal = false;

        ServerWorld target = source.getServer().getWorld(
                source.getRegistryKey().equals(ModDimensions.CLASSIC_LEVEL_KEY)
                        ? ModDimensions.CLASSIC_NETHER_LEVEL_KEY
                        : ModDimensions.CLASSIC_LEVEL_KEY);

        if (target != null && !this.hasVehicle() && this.netherPortalTime++ >= this.getMaxNetherPortalTime()) {
            this.netherPortalTime = this.getMaxNetherPortalTime();
            this.resetPortalCooldown();
            gallifrey$teleport((Entity) (Object) this, source, target);
        }
        this.tickPortalCooldown();
    }

    private void gallifrey$teleport(Entity entity, ServerWorld source, ServerWorld target) {
        BlockPos from = this.lastNetherPortalPosition != null ? this.lastNetherPortalPosition : entity.getBlockPos();
        Direction.Axis axis = source.getBlockState(from).getOrEmpty(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);

        // Same X/Z on both sides, clamped into the target's buildable height.
        BlockPos dest = target.getWorldBorder().clamp(entity.getX(), entity.getY(), entity.getZ());
        int y = Math.max(target.getBottomY() + 2, Math.min(dest.getY(), target.getBottomY() + target.getLogicalHeight() - 6));
        dest = new BlockPos(dest.getX(), y, dest.getZ());

        boolean toNether = target.getRegistryKey().equals(ModDimensions.CLASSIC_NETHER_LEVEL_KEY);
        Optional<BlockLocating.Rectangle> portal = target.getPortalForcer().getPortalRect(dest, toNether, target.getWorldBorder());
        if (portal.isEmpty()) {
            portal = target.getPortalForcer().createPortal(dest, axis);
            portal.ifPresent(rect -> gallifrey$makeFrameClassic(target, rect));
        }

        Vec3d pos = portal
                .map(rect -> new Vec3d(rect.lowerLeft.getX() + 0.5D, rect.lowerLeft.getY(), rect.lowerLeft.getZ() + 0.5D))
                .orElse(Vec3d.ofBottomCenter(dest));

        FabricDimensions.teleport(entity, target, new TeleportTarget(pos, Vec3d.ZERO, entity.getYaw(), entity.getPitch()));
    }

    /** Swap the vanilla obsidian vanilla just built (frame + any platform) for Classic Obsidian. */
    private static void gallifrey$makeFrameClassic(ServerWorld world, BlockLocating.Rectangle rect) {
        BlockPos ll = rect.lowerLeft;
        for (BlockPos p : BlockPos.iterate(ll.add(-2, -2, -2), ll.add(rect.width + 2, rect.height + 1, rect.width + 2))) {
            if (world.getBlockState(p).isOf(Blocks.OBSIDIAN)) {
                world.setBlockState(p, GallifreyModBlocks.CLASSIC_OBSIDIAN.getDefaultState(),
                        Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            }
        }
    }

    private static boolean gallifrey$isClassic(RegistryKey<World> key) {
        return key.equals(ModDimensions.CLASSIC_LEVEL_KEY) || key.equals(ModDimensions.CLASSIC_NETHER_LEVEL_KEY);
    }
}
