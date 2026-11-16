package com.cerulixteam.gallifrey.mixin.structureblock;

import com.cerulixteam.gallifrey.structureblock.HugeStructureBlocks;
import net.minecraft.server.command.PlaceCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** /place jigsaw can go deeper than vanilla's 7. */
@Mixin(PlaceCommand.class)
public abstract class PlaceCommandDepthMixin {
    @ModifyConstant(method = "register", constant = @Constant(intValue = 7))
    private static int gallifrey$maxDepth(int vanilla) {
        return HugeStructureBlocks.MAX_JIGSAW_DEPTH;
    }
}
