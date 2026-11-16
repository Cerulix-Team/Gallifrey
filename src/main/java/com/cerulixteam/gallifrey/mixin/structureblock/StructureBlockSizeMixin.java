package com.cerulixteam.gallifrey.mixin.structureblock;

import com.cerulixteam.gallifrey.structureblock.HugeStructureBlocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.stream.Stream;

/** Structure blocks up to HugeStructureBlocks.LIMIT in size and offset, finding their corners from as far. */
@Mixin(StructureBlockBlockEntity.class)
public abstract class StructureBlockSizeMixin {
    @ModifyConstant(method = "readNbt", constant = @Constant(intValue = 48))
    private int gallifrey$maxSize(int vanilla) {
        return HugeStructureBlocks.LIMIT;
    }

    @ModifyConstant(method = "readNbt", constant = @Constant(intValue = -48))
    private int gallifrey$minOffset(int vanilla) {
        return -HugeStructureBlocks.LIMIT;
    }

    @ModifyConstant(method = "detectStructureSize", constant = @Constant(intValue = 80))
    private int gallifrey$detectRange(int vanilla) {
        return HugeStructureBlocks.LIMIT;
    }

    /**
     * Vanilla checks every block in the box for corner blocks; over 512 blocks each way that's far too many,
     * so this looks only at the structure blocks the world knows about, nearest first (same result).
     */
    @Inject(method = "streamCornerPos", at = @At("HEAD"), cancellable = true)
    private void gallifrey$knownCorners(BlockPos start, BlockPos end, CallbackInfoReturnable<Stream<BlockPos>> cir) {
        StructureBlockBlockEntity self = (StructureBlockBlockEntity) (Object) this;
        World world = self.getWorld();
        String name = self.getTemplateName();
        if (world == null || world.isClient || name == null) return;
        BlockPos here = self.getPos();
        int minX = Math.min(start.getX(), end.getX()), maxX = Math.max(start.getX(), end.getX());
        int minY = Math.min(start.getY(), end.getY()), maxY = Math.max(start.getY(), end.getY());
        int minZ = Math.min(start.getZ(), end.getZ()), maxZ = Math.max(start.getZ(), end.getZ());
        cir.setReturnValue(HugeStructureBlocks.all(world).stream()
                .filter(p -> p.getX() >= minX && p.getX() <= maxX && p.getY() >= minY && p.getY() <= maxY && p.getZ() >= minZ && p.getZ() <= maxZ)
                .filter(p -> !p.equals(here))
                .filter(p -> {
                    BlockEntity be = world.getBlockEntity(p);
                    return be instanceof StructureBlockBlockEntity sb && sb.getMode() == StructureBlockMode.CORNER && name.equals(sb.getTemplateName());
                })
                .sorted(Comparator.comparingDouble(p -> p.getSquaredDistance(here))));
    }
}
