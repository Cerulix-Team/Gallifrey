package com.cerulixteam.gallifrey.mixin.backport;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** FoliagePlacerType's constructor is private in 1.20.1; this lets the poplar foliage placer register its own type. */
@Mixin(FoliagePlacerType.class)
public interface FoliagePlacerTypeInvoker {
    @Invoker("<init>")
    static <P extends FoliagePlacer> FoliagePlacerType<P> gallifrey$create(Codec<P> codec) {
        throw new AssertionError();
    }
}
