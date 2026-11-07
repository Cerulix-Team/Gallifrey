package com.cerulixteam.gallifrey.mixin.structureblock;

import com.cerulixteam.gallifrey.structureblock.HugeStructureBlocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps HugeStructureBlocks' per-world list of structure blocks up to date (server side only). */
@Mixin(BlockEntity.class)
public abstract class StructureBlockTrackerMixin {
    @Inject(method = "setWorld", at = @At("TAIL"))
    private void gallifrey$track(World world, CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self instanceof StructureBlockBlockEntity && !world.isClient) HugeStructureBlocks.add(world, self.getPos());
    }

    @Inject(method = "cancelRemoval", at = @At("TAIL"))
    private void gallifrey$retrack(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self instanceof StructureBlockBlockEntity && self.getWorld() != null && !self.getWorld().isClient) HugeStructureBlocks.add(self.getWorld(), self.getPos());
    }

    @Inject(method = "markRemoved", at = @At("HEAD"))
    private void gallifrey$untrack(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self instanceof StructureBlockBlockEntity && self.getWorld() != null && !self.getWorld().isClient) HugeStructureBlocks.remove(self.getWorld(), self.getPos());
    }
}
