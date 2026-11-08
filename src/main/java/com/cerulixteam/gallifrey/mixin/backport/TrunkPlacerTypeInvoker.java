package com.cerulixteam.gallifrey.mixin.backport;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** TrunkPlacerType's constructor is private in 1.20.1; this lets the poplar trunk placer register its own type. */
@Mixin(TrunkPlacerType.class)
public interface TrunkPlacerTypeInvoker {
    @Invoker("<init>")
    static <P extends TrunkPlacer> TrunkPlacerType<P> gallifrey$create(Codec<P> codec) {
        throw new AssertionError();
    }
}
