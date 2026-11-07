package com.cerulixteam.gallifrey.mixin.structureblock;

import com.cerulixteam.gallifrey.structureblock.HugeStructureBlocks;
import net.minecraft.world.gen.structure.JigsawStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Jigsaw structures in data packs may be bigger: "size" (depth) up to HugeStructureBlocks.MAX_JIGSAW_DEPTH
 * and "max_distance_from_center" up to HugeStructureBlocks.LIMIT (vanilla: 7 and 128). The Trial Chambers
 * (size 20) need this. method_41662 is the lambda that builds JigsawStructure's codec.
 */
@Mixin(JigsawStructure.class)
public abstract class JigsawLimitsMixin {
    @ModifyConstant(method = "method_41662", constant = @Constant(intValue = 7))
    private static int gallifrey$maxDepth(int vanilla) {
        return HugeStructureBlocks.MAX_JIGSAW_DEPTH;
    }

    @ModifyConstant(method = "method_41662", constant = @Constant(intValue = 128))
    private static int gallifrey$maxDistance(int vanilla) {
        return HugeStructureBlocks.LIMIT;
    }

    @ModifyConstant(method = "validate", constant = @Constant(intValue = 128))
    private static int gallifrey$maxDistanceWithTerrain(int vanilla) {
        return HugeStructureBlocks.LIMIT;
    }
}
