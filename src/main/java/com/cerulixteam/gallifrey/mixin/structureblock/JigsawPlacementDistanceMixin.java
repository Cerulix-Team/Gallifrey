package com.cerulixteam.gallifrey.mixin.structureblock;

import com.cerulixteam.gallifrey.structureblock.HugeStructureBlocks;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** /place jigsaw and jigsaw blocks' Generate button may build further from the start (vanilla: 128). */
@Mixin(StructurePoolBasedGenerator.class)
public abstract class JigsawPlacementDistanceMixin {
    @ModifyConstant(method = "generate(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/util/Identifier;ILnet/minecraft/util/math/BlockPos;Z)Z",
            constant = @Constant(intValue = 128))
    private static int gallifrey$maxDistance(int vanilla) {
        return HugeStructureBlocks.LIMIT;
    }
}
