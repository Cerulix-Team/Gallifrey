package com.cerulixteam.gallifrey.mixin.client.structureblock;

import com.cerulixteam.gallifrey.structureblock.HugeStructureBlocks;
import net.minecraft.client.render.block.entity.StructureBlockBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Huge structure blocks' outlines stay visible from further away. */
@Mixin(StructureBlockBlockEntityRenderer.class)
public abstract class StructureBlockRenderDistanceMixin {
    @ModifyConstant(method = "getRenderDistance", constant = @Constant(intValue = 96))
    private int gallifrey$renderDistance(int vanilla) {
        return HugeStructureBlocks.RENDER_DISTANCE;
    }
}
