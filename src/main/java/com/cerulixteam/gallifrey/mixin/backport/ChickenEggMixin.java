package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.mob.AnimalVariants;
import com.cerulixteam.gallifrey.block.backport.mob.BackportMobs;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.ItemConvertible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Warm chickens lay Brown Eggs and cold chickens lay Blue Eggs (1.21.5). */
@Mixin(ChickenEntity.class)
public abstract class ChickenEggMixin {
    @ModifyArg(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/passive/ChickenEntity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;"))
    private ItemConvertible gallifrey$variantEgg(ItemConvertible egg) {
        return switch (AnimalVariants.get((ChickenEntity) (Object) this)) {
            case AnimalVariants.WARM -> BackportMobs.BROWN_EGG;
            case AnimalVariants.COLD -> BackportMobs.BLUE_EGG;
            default -> egg;
        };
    }
}
